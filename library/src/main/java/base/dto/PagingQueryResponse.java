package base.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

/**
 * @author csieflyman
 */
@Getter
@ToString
public class PagingQueryResponse<T> extends Response<List<T>> {

    private Long total;
    private Integer pageSize = 10;
    private Integer pageNo = 1;
    private Integer pages = 1;

    public PagingQueryResponse(Integer pageSize, Integer pageNo, List<T> result) {
        this((long) result.size(), pageSize, pageNo, result);
        pagingResult(result);
    }

    public PagingQueryResponse(Long total, Integer pageSize, Integer pageNo, List<T> result) {
        super(result);
        setPaging(total, pageNo, pageSize);
    }

    @JsonCreator
    public PagingQueryResponse(@JsonProperty("code") String code, @JsonProperty("message") String message, @JsonProperty("result") JsonNode jsonNode,
                               @JsonProperty("total") Integer total, @JsonProperty("pageNo") Integer pageNo, @JsonProperty("pageSize") Integer pageSize) {
        super(code, message, jsonNode);
        setPaging((long)total, pageNo, pageSize);
    }

    private void setPaging(Long total, Integer pageNo, Integer pageSize) {
        if(total != null) {
            Preconditions.checkArgument(total >= 0);
            this.total = total;
        }
        if(pageSize != null) {
            Preconditions.checkArgument(pageSize >= 1 && pageSize <= 100);
            this.pageSize = pageSize;
        }
        if(pageNo != null) {
            Preconditions.checkArgument(pageNo >= 1);
            this.pageNo = pageNo;
        }
        this.pages = (int)(long)(this.total / this.pageSize) + ((this.total % this.pageSize) == 0 ? 0 : 1);
    }

    private void pagingResult(List<T> result) {
        int startIndex = pageSize * (pageNo - 1);
        if(startIndex >= result.size()) {
            this.result = Collections.emptyList();
        }
        else {
            int endIndex = pageSize * pageNo;
            if (endIndex > result.size()) {
                endIndex = result.size();
            }
            this.result = result.subList(startIndex, endIndex);
        }
    }
}
