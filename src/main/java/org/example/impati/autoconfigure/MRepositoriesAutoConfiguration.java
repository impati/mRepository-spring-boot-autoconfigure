package org.example.impati.autoconfigure;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.List;
import org.example.impati.core.MRepository;
import org.example.impati.core.method_invoker.MRepositoryMethodInvoker;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.ResolvableType;
import org.springframework.core.type.filter.AssignableTypeFilter;

@AutoConfiguration
@ConditionalOnClass(MRepository.class)
public class MRepositoriesAutoConfiguration {

    @Bean
    public BeanDefinitionRegistryPostProcessor mRepositoriesRegistrar(List<MRepositoryMethodInvoker<?>> methodInvokers) {

        return new BeanDefinitionRegistryPostProcessor() {
            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
                // 1) 스캔 기준 패키지 결정
                List<String> bases = new ArrayList<>();
                if (registry instanceof DefaultListableBeanFactory dlbf && AutoConfigurationPackages.has(dlbf)) {
                    List<String> c = AutoConfigurationPackages.get(dlbf);
                    bases.addAll(c); // @SpringBootApplication 패키지들
                } else {
                    return; // 기준이 없으면 스킵 (원하면 예외로 바꿔도 됨)
                }

                // 2) 인터페이스 스캐너 (컴포넌트 스캔 아님)
                var scanner = new ClassPathScanningCandidateComponentProvider(false) {
                    @Override
                    protected boolean isCandidateComponent(AnnotatedBeanDefinition bd) {
                        // 인터페이스도 후보로 인정
                        var md = bd.getMetadata();
                        return md.isIndependent(); // (기존 기본값은 concrete class 위주)
                    }
                };
                scanner.addIncludeFilter(new AssignableTypeFilter(MRepository.class));

                // 3) 후보 인터페이스 → FactoryBean 등록
                for (String base : bases) {
                    for (BeanDefinition cand : scanner.findCandidateComponents(base)) {
                        try {
                            Class<?> repoItf = Class.forName(cand.getBeanClassName());
                            if (!repoItf.isInterface() || repoItf == MRepository.class) {
                                continue;
                            }

                            Class<?> entityType = resolveEntityType(repoItf);
                            if (entityType == null) {
                                throw new IllegalArgumentException("Cannot resolve <E> for " + repoItf.getName());
                            }

                            AbstractBeanDefinition beanDefinition;
                            if (methodInvokers.isEmpty()) {
                                beanDefinition = BeanDefinitionBuilder
                                        .genericBeanDefinition(MRepositoryFactoryBean.class)
                                        .addConstructorArgValue(repoItf)
                                        .getBeanDefinition();
                            } else {
                                beanDefinition = BeanDefinitionBuilder
                                        .genericBeanDefinition(MRepositoryFactoryBean.class)
                                        .addConstructorArgValue(repoItf)
                                        .addConstructorArgValue(methodInvokers)
                                        .getBeanDefinition();
                            }

                            String beanName = Introspector.decapitalize(repoItf.getSimpleName());
                            if (!registry.containsBeanDefinition(beanName)) {
                                registry.registerBeanDefinition(beanName, beanDefinition);
                            }
                        } catch (ClassNotFoundException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                }
            }

            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory bf) {
            }

            private Class<?> resolveEntityType(Class<?> repoInterface) {
                // repoInterface 가 MRepository<K,E>를 직접/간접 구현한다고 가정
                ResolvableType rt = ResolvableType.forClass(repoInterface).as(MRepository.class);
                if (rt.hasGenerics()) {
                    Class<?> e = rt.getGeneric(1).resolve(); // K,E 중 1 = E
                    if (e != null) {
                        return e;
                    }
                }
                for (Class<?> itf : repoInterface.getInterfaces()) {
                    Class<?> e = resolveEntityType(itf);
                    if (e != null) {
                        return e;
                    }
                }
                return null;
            }
        };
    }
}
