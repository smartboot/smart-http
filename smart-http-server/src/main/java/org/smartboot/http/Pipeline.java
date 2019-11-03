package org.smartboot.http;

import org.smartboot.http.server.handle.HttpHandle;

/**
 * @author 三刀
 * @version V1.0 , 2019/11/3
 */
public interface Pipeline {
    Pipeline next(HttpHandle nextHandle);
}
