package base.util;

import base.exception.ConversionException;
import base.exception.InternalServerErrorException;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * @author csieflyman
 */
public class BeanUtils {

    private BeanUtils() {

    }

    static {
        BeanUtilsBean.setInstance(new BeanUtilsBean(new MyConvertUtilsBean()));
        ConvertUtilsBean converter = BeanUtilsBean.getInstance().getConvertUtils();
        converter.register(new DateTimeConverter(), Date.class);
        converter.register(new DateTimeConverter(), java.sql.Timestamp.class);
    }

    public static <T> T convert(Object value, Class targetType) {
        T result;
        try {
            result = (T) BeanUtilsBean.getInstance().getConvertUtils().convert(value, targetType);
        } catch (org.apache.commons.beanutils.ConversionException e) {
            throw new ConversionException(String.format("convert failure from %s (%s) to %s", value.toString(), value.getClass().getName(), targetType.getName()), e);
        }
        return result;
    }

    public static void copyIgnoreNull(Object source, Object target) {
        copyIgnoreNullAndProps(source, target, new HashSet<>());
    }

    public static void copyIgnoreNullAndProps(Object source, Object target, Set<String> ignoreFields) {
        Set<String> fieldNames = new HashSet<>();
        String[] ignoreFieldNames = Stream.of(source.getClass().getFields()).filter(field -> {
            try {
                if(fieldNames.contains(field.getName())) // 略過 superclass 重複名稱的欄位
                    return false;
                boolean ignore = ignoreFields.contains(field.getName()) || (Modifier.isPublic(field.getModifiers()) && field.get(source) == null);
                fieldNames.add(field.getName());
                return ignore;
            } catch (IllegalAccessException e) {
                throw new InternalServerErrorException(String.format("fail to copy field %s",
                        source.getClass().getName() + "[" + field.getName() + "]"), e);
            }
        }).map(Field::getName).toArray(String[]::new);
        org.springframework.beans.BeanUtils.copyProperties(source, target, ignoreFieldNames);
    }

    private static class MyConvertUtilsBean extends org.apache.commons.beanutils.ConvertUtilsBean {

        private static final Converter enumConverter = new EnumConverter();
        private static final Converter uuidConverter = new UUIDConverter();

        @Override
        public Converter lookup(Class<?> clazz) {
            if(clazz.isEnum()) {
                return enumConverter;
            }
            else if(clazz == UUID.class) {
                return uuidConverter;
            }
            return super.lookup(clazz);
        }
    }

    private static class EnumConverter extends org.apache.commons.beanutils.converters.AbstractConverter {

        private EnumConverter() {
            super(null);
        }

        @Override
        protected String convertToString(Object value) {
            return ((Enum)value).name();
        }

        @Override
        protected Enum convertToType(Class type, Object value) {
            if(value.getClass().isEnum()) {
                return (Enum) value;
            }
            else if(value instanceof String) {
                return Enum.valueOf(type, (String) value);
            }
            else {
                return (Enum) type.getEnumConstants()[(Integer)value];
            }
        }

        @Override
        protected Class getDefaultType() {
            return Enum.class;
        }
    }

    private static class UUIDConverter extends org.apache.commons.beanutils.converters.AbstractConverter {

        private UUIDConverter() {
            super(null);
        }

        @Override
        public <T> T convert(Class<T> type, Object value) {
            if(value instanceof UUID) {
                return (T) value;
            }
            else if(value instanceof String) {
                return (T) UUID.fromString((String)value);
            }
            else if (value instanceof byte[]) {
                return (T) UUID.nameUUIDFromBytes((byte[]) value);
            }
            else {
                throw new ConversionException(String.format("%s %s can't convert to UUID", value.getClass(), value.toString()));
            }
        }

        @Override
        protected String convertToString(Object value) {
            return ((UUID) value).toString();
        }

        @Override
        protected UUID convertToType(Class type, Object value) {
            return null;
        }

        @Override
        protected Class getDefaultType() {
            return UUID.class;
        }
    }

    private static class DateTimeConverter extends org.apache.commons.beanutils.converters.AbstractConverter {

        private DateTimeConverter() {
            super(null);
        }

        @Override
        protected String convertToString(Object value) {
            return DateTimeUtils.UTC_DATE_TIME_FORMATTER.format(((Date) value).toInstant());
        }

        @Override
        protected Date convertToType(Class type, Object value) {
            if(Date.class.isAssignableFrom(value.getClass())) {
                return (Date)value;
            }
            return Date.from(ZonedDateTime.parse((String) value, DateTimeUtils.UTC_DATE_TIME_FORMATTER).toInstant());
        }

        @Override
        protected Class getDefaultType() {
            return Date.class;
        }
    }
}
