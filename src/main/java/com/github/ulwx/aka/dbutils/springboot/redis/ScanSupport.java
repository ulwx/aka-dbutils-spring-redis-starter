package com.github.ulwx.aka.dbutils.springboot.redis;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ScanSupport {
    private  static Logger log = Logger.getLogger(ScanSupport.class);
    public  static ScanSupport instance=new ScanSupport();
    private final ResourceLoader resourceLoader = new PathMatchingResourcePatternResolver();
    private final ResourcePatternResolver resolver  = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
    private final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourceLoader);


    public Resource[] getResources(String scanPath)throws IOException {
        Resource[] resources = resolver.getResources(scanPath);
        return resources;
    }
    /**
     * 根据条件扫描类
     * @return
     * @throws IOException
     */
    public Set<Class<?>> doScan(String scanPath,String classNamePrefix,
                                String classNameSufix,Class<?> parent) throws IOException {
        Set<Class<?>> classes = new HashSet<>();
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                .concat(ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(scanPath))
                        .concat("/**/" +classNamePrefix+
                                "*" +classNameSufix+
                                ".class"));
        Resource[] resources = resolver.getResources(packageSearchPath);
        MetadataReader metadataReader = null;
        for (Resource resource : resources) {
            if (resource.isReadable()) {
                metadataReader = metadataReaderFactory.getMetadataReader(resource);
                try {
                    Class clazz=Class.forName(metadataReader.getClassMetadata().getClassName());
                    if(parent!=null){
                        if(parent.isAssignableFrom(clazz)){
                            classes.add(clazz);
                        }
                    }else{
                        classes.add(clazz);
                    }

                } catch (Exception e) {
                    log.error(""+e, e);
                }
            }
        }
        return classes;
    }

}
