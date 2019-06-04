package base.controller;

import base.dto.PagingQueryResponse;
import base.exception.BadRequestException;
import base.model.BaseModel;
import base.service.GenericService;
import base.util.Json;
import base.util.http.BodyCachingHttpServletRequestWrapper;
import base.util.query.Query;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author csieflyman
 */
@Slf4j
@RestController
public abstract class AbstractController {

    @Autowired
    protected HttpServletRequest request;

    protected void processBindingResult(BindingResult result) {
        if (result.hasErrors()) {
            String body = null;
            try {
                BodyCachingHttpServletRequestWrapper requestWrapper = new BodyCachingHttpServletRequestWrapper(request);
                body = new String(requestWrapper.getBody(), Charset.forName("UTF-8"));
            } catch (IOException e ) {
                log.warn("fail to get request body: " + request.getMethod() + " " + request.getRequestURI());
            }
            throw new BadRequestException(result.getFieldErrors().stream()
                    .map(error -> error.getField() + " : " + error.getDefaultMessage()).collect(Collectors.joining(";")), body);
        }
    }

    protected <T extends BaseModel<ID>, ID> ResponseEntity findEntities(GenericService<T, ID> genericService, Query query) {
        return findEntities(genericService, query, null);
    }

    protected <T extends BaseModel<ID>, ID> ResponseEntity findEntities(GenericService<T, ID> genericService, Query query, Function<T, JsonNode> toJsonFunction) {
        return findEntities(genericService::findSize, genericService::find, query, toJsonFunction);
    }

    protected <T extends BaseModel> ResponseEntity findEntities(Function<Query, Long> findSizeFunction, Function<Query, List<T>> findFunction, Query query, Function<T, JsonNode> toJsonFunction) {
        if(query == null) {
            query = Query.create(request.getParameterMap());
        }

        if (query.isOnlySize()) {
            return ResponseEntity.ok(String.valueOf(findSizeFunction.apply(query)));
        }
        else {
            List<T> entities = findFunction.apply(query);
            List<JsonNode> nodes = entities.stream().map(e -> toJsonFunction == null ? Json.toJsonNode(e) : toJsonFunction.apply(e)).collect(Collectors.toList());
            if(query.isPagingQuery()) {
                long total = findSizeFunction.apply(query);
                return ResponseEntity.ok(Json.toJsonNode(new PagingQueryResponse<>(total, query.getPageSize(), query.getPageNo(), nodes)));
            }
            else {
                return ResponseEntity.ok(Json.newArray().addAll(nodes));
            }
        }
    }
}