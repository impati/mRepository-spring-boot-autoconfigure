package org.example.impati.autoconfigure;

import java.lang.reflect.Method;
import java.util.List;
import org.example.impati.core.MStore;
import org.example.impati.core.method_invoker.DeleteInvoker;
import org.example.impati.core.method_invoker.MRepositoryMethodInvoker;
import org.example.impati.utils.CollectionUtils;
import org.springframework.context.ApplicationEventPublisher;

public class DeleteInvokerWithBackup<E> implements MRepositoryMethodInvoker<E> {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final DeleteInvoker<E> deleteInvoker;

    protected DeleteInvokerWithBackup(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.deleteInvoker = new DeleteInvoker<>();
    }

    @Override
    public boolean supports(final Method method) {
        return deleteInvoker.supports(method);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(final MStore<Object, E> store, final Method method, final Object[] args) {
        Object obj = args[0];
        if (obj instanceof List<?>) {
            List<E> items = (List<E>) CollectionUtils.toCollection(obj);
            deleteInvoker.invoke(store, method, args);
            applicationEventPublisher.publishEvent(new MEvents<>(items, MEventType.DELETE));
            return "";
        }

        E item = store.save((E) obj);
        deleteInvoker.invoke(store, method, args);
        applicationEventPublisher.publishEvent(new MEvent<>(item, MEventType.DELETE));
        return item;
    }
}
