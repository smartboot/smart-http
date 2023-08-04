package org.smartboot.http.restful.context;

import java.util.List;

/**
 * @author qinluo
 * @date 2023-08-02 11:25:46
 * @since 1.2.8
 */
public interface BeanScanner {

    /**
     * Scan and register bean.
     *
     * @param ctx      ctx.
     * @param packages scanned packages.
     */
    void scanAndRegister(ApplicationContext ctx, List<String> packages);
}
