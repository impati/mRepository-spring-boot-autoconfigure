package org.example.impati.autoconfigure;

import java.util.List;
import org.example.impati.core.MRepository;
import org.example.impati.core.MRepositoryFactory;
import org.example.impati.core.method_invoker.MRepositoryMethodInvoker;
import org.springframework.beans.factory.FactoryBean;

public class MRepositoryFactoryBean<K, E, T extends MRepository<K, E>> implements FactoryBean<T> {

    private final Class<T> repoInterface;
    private final List<MRepositoryMethodInvoker<E>> methodInvokers;
    private T proxy;

    public MRepositoryFactoryBean(Class<T> repoInterface, List<MRepositoryMethodInvoker<E>> methodInvokers) {
        this.repoInterface = repoInterface;
        this.methodInvokers = methodInvokers;
    }

    public MRepositoryFactoryBean(Class<T> repoInterface) {
        this.repoInterface = repoInterface;
        this.methodInvokers = List.of();
    }

    @Override
    public T getObject() {
        if (proxy == null) {
            if (methodInvokers.isEmpty()) {
                return MRepositoryFactory.create(repoInterface);
            }

            return MRepositoryFactory.create(repoInterface, methodInvokers);
        }

        return proxy;
    }

    @Override
    public Class<?> getObjectType() {
        return repoInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
