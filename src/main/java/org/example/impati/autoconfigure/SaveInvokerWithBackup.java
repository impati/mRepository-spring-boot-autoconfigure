package org.example.impati.autoconfigure;

import java.lang.reflect.Method;
import java.util.List;
import org.example.impati.core.MStore;
import org.example.impati.core.method_invoker.MRepositoryMethodInvoker;
import org.example.impati.core.method_invoker.SaveInvoker;
import org.example.impati.utils.CollectionUtils;
import org.springframework.context.ApplicationEventPublisher;

public class SaveInvokerWithBackup<E> implements MRepositoryMethodInvoker<E> {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final SaveInvoker<E> saveInvoker;

    protected SaveInvokerWithBackup(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.saveInvoker = new SaveInvoker<>();
    }

    @Override
    public boolean supports(final Method method) {
        return saveInvoker.supports(method);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(final MStore<Object, E> store, final Method method, final Object[] args) {
        Object obj = args[0];
        if (obj instanceof List<?>) {
            List<E> items = (List<E>) CollectionUtils.toCollection(obj);
            saveInvoker.invoke(store, method, args);
            MEvents<E> event = new MEvents<>(items, MEventType.SAVE);
            applicationEventPublisher.publishEvent(event);
            return "";
        }

        E item = store.save((E) obj);
        saveInvoker.invoke(store, method, args);
        applicationEventPublisher.publishEvent(new MEvent<>(item, MEventType.SAVE));
        return item;
    }
}
