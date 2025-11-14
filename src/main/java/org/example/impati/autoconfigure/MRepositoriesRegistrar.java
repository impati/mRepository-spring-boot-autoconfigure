package org.example.impati.autoconfigure;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.example.impati.core.MRepository;
import org.example.impati.core.backup.BackupEntityLoader;
import org.example.impati.core.method_invoker.MRepositoryMethodInvoker;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AssignableTypeFilter;

public class MRepositoriesRegistrar implements BeanDefinitionRegistryPostProcessor {

    private final ObjectProvider<List<MRepositoryMethodInvoker<?>>> methodInvokersProvider;
    private final Environment environment;

    public MRepositoriesRegistrar(ObjectProvider<List<MRepositoryMethodInvoker<?>>> methodInvokersProvider, Environment environment) {
        this.methodInvokersProvider = methodInvokersProvider;
        this.environment = environment;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        List<MRepositoryMethodInvoker<?>> methodInvokers =
                methodInvokersProvider.getIfAvailable(Collections::emptyList);

        boolean backupEnable = environment.getProperty(
                "m.repository.backup-enable",
                Boolean.class,
                false
        );

        // 1) 기준 패키지
        List<String> bases = new ArrayList<>();
        if (registry instanceof DefaultListableBeanFactory dlbf && AutoConfigurationPackages.has(dlbf)) {
            bases.addAll(AutoConfigurationPackages.get(dlbf));
        } else {
            return;
        }

        // 2) MRepository 인터페이스 스캔
        var scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition bd) {
                return bd.getMetadata().isIndependent();
            }
        };
        scanner.addIncludeFilter(new AssignableTypeFilter(MRepository.class));

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

                    // 3) MRepositoryFactoryBean 등록
                    AbstractBeanDefinition repoDef;
                    if (methodInvokers.isEmpty()) {
                        repoDef = BeanDefinitionBuilder
                                .genericBeanDefinition(MRepositoryFactoryBean.class)
                                .addConstructorArgValue(repoItf)
                                .getBeanDefinition();
                    } else {
                        repoDef = BeanDefinitionBuilder
                                .genericBeanDefinition(MRepositoryFactoryBean.class)
                                .addConstructorArgValue(repoItf)
                                .addConstructorArgValue(methodInvokers)
                                .getBeanDefinition();
                    }

                    String repoBeanName = Introspector.decapitalize(repoItf.getSimpleName());
                    if (!registry.containsBeanDefinition(repoBeanName)) {
                        registry.registerBeanDefinition(repoBeanName, repoDef);
                    }

                    // 4) backup이 켜져 있으면 BackupEntityLoader<K,E>도 함께 등록
                    if (backupEnable) {
                        String loaderBeanName = repoBeanName + "BackupEntityLoader";

                        AbstractBeanDefinition loaderDef =
                                BeanDefinitionBuilder
                                        .genericBeanDefinition(BackupEntityLoader.class)
                                        .addConstructorArgValue(entityType)       // Class<E>
                                        .addConstructorArgReference(repoBeanName) // MRepository<K,E>
                                        .addConstructorArgReference("backupRepository")
                                        .getBeanDefinition();

                        if (!registry.containsBeanDefinition(loaderBeanName)) {
                            registry.registerBeanDefinition(loaderBeanName, loaderDef);
                        }
                    }
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        // noop
    }

    private Class<?> resolveEntityType(Class<?> repoInterface) {
        ResolvableType rt = ResolvableType.forClass(repoInterface).as(MRepository.class);
        if (rt.hasGenerics()) {
            Class<?> e = rt.getGeneric(1).resolve(); // <K,E> 중 E
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
}

