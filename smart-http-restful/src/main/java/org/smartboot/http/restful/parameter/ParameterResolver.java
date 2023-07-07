package org.smartboot.http.restful.parameter;

import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;

/**
 * @author qinluo
 * @date 2023-07-07 15:37:38
 * @since 1.0.0
 */
public interface ParameterResolver {

    Object resolve(ParameterMetadata metadata, HttpRequest request, HttpResponse response);
}
