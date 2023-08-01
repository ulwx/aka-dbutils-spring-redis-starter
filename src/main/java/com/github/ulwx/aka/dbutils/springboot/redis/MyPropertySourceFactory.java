package com.github.ulwx.aka.dbutils.springboot.redis;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class MyPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource rs) throws IOException {

        String resourceName = Optional.ofNullable(name).orElse(rs.getResource().getFilename());
        Map<String, Object> sourceMap=new LinkedHashMap<>();

        Resource[]  resources=ScanSupport.instance.getResources(resourceName);
        for(int i=0; i<resources.length; i++) {
            if (!resources[i].exists()) {
                throw new IllegalArgumentException("Resource " + resourceName + " does not exist");
            }
            Resource resource=resources[i];
            PropertiesPropertySource propertySource = null;
            if (resourceName.endsWith(".yml") || resourceName.endsWith(".yaml")) {
                YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
                factory.setResources(resource);
                factory.afterPropertiesSet();
                propertySource = new PropertiesPropertySource(resourceName, factory.getObject());

            }else{
                rs=new EncodedResource(resource, rs.getEncoding());
                propertySource= (name != null ?
                        new ResourcePropertySource(name, rs) : new ResourcePropertySource(rs));
            }
            sourceMap.putAll(propertySource.getSource());
        }
        MapPropertySource finalPropertiesPropertySource =
                new MapPropertySource(MyPropertySourceFactory.class.getName()+"."
                        +resourceName,sourceMap);
        return  finalPropertiesPropertySource;


    }


}
