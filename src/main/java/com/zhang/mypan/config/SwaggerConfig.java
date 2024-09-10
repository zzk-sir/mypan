package com.zhang.mypan.config;


//@Configuration
//@EnableOpenApi
public class SwaggerConfig {
    /**
     * 用于读取配置文件 application.properties 中 swagger 属性是否开启
     */
//    private ApiInfo apiInfo() {
//        /*作者信息*/
//        Contact contact = new Contact("YYY", "url", "xxx@qq.com");
//        return new ApiInfo(
//                "myPan项目",
//                "Swagger3 测试接口文档",
//                "v1.0",
//                "url1",
//                contact,
//                "Apache 2.0",
//                "http://www.apache.org/licenses/LICENSE-2.0",
//                new ArrayList<>()
//        );
//    }
////    @Bean
//    public static BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
//        return new BeanPostProcessor() {
//
//            @Override
//            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
//                if (bean instanceof WebMvcRequestHandlerProvider || bean instanceof WebFluxRequestHandlerProvider) {
//                    customizeSpringfoxHandlerMappings(getHandlerMappings(bean));
//                }
//                return bean;
//            }
//
//            private <T extends RequestMappingInfoHandlerMapping> void customizeSpringfoxHandlerMappings(List<T> mappings) {
//                List<T> copy = mappings.stream()
//                        .filter(mapping -> mapping.getPatternParser() == null)
//                        .collect(Collectors.toList());
//                mappings.clear();
//                mappings.addAll(copy);
//            }
//
//            @SuppressWarnings("unchecked")
//            private List<RequestMappingInfoHandlerMapping> getHandlerMappings(Object bean) {
//                try {
//                    Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
//                    field.setAccessible(true);
//                    return (List<RequestMappingInfoHandlerMapping>) field.get(bean);
//                } catch (IllegalArgumentException | IllegalAccessException e) {
//                    throw new IllegalStateException(e);
//                }
//            }
//        };
//    }
}