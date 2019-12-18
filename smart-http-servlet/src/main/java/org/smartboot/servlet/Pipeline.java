package org.smartboot.servlet;

import org.smartboot.servlet.handler.Handler;

/**
 * @author 三刀
 * @version V1.0 , 2019/11/3
 */
public interface Pipeline {
    Pipeline next(Handler nextHandle);
}
