package org.smartboot.http.restful.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.smartboot.http.restful.context.Expand;
import org.smartboot.http.restful.context.ApplicationContext;

import java.lang.reflect.Proxy;
import java.util.List;

public class MyBatisExpand implements Expand<Mapper> {

    @Override
    public void initializeBean(ApplicationContext context, List<Class<Mapper>> mappers) throws Exception {
        SqlSessionFactory factory = context.getBean("sessionFactory");
        for (Class<Mapper> mapperClass : mappers) {
            context.addBean(mapperClass.getSimpleName().substring(0, 1).toLowerCase() + mapperClass.getSimpleName().substring(1), Proxy.newProxyInstance(mapperClass.getClassLoader(), new Class[]{mapperClass}, (proxy, method, args) -> {
                try (SqlSession session = factory.openSession(true)) {
                    return method.invoke(session.getMapper(mapperClass), args);
                }
            }));
        }
    }

    @Override
    public Class<Mapper> expandAnnotation() {
        return Mapper.class;
    }
}
