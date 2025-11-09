package org.example.impati.autoconfigure;

import org.example.impati.core.MRepository;
import org.example.impati.core.MRepositoryFactory;
import org.springframework.beans.factory.FactoryBean;

public class MRepositoryFactoryBean<K, E, T extends MRepository<K, E>> implements FactoryBean<T> {

    private final Class<T> repoInterface;
    private T proxy;

    public MRepositoryFactoryBean(Class<T> repoInterface) {
        this.repoInterface = repoInterface;
    }

    @Override
    public T getObject() {
        if (proxy == null) {
            return MRepositoryFactory.create(repoInterface);
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
