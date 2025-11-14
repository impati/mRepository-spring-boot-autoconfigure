package org.example.impati.autoconfigure;

import java.util.List;
import org.example.impati.core.MRepository;
import org.example.impati.core.method_invoker.MRepositoryMethodInvoker;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@AutoConfiguration
@ConditionalOnClass(MRepository.class)
public class MRepositoriesAutoConfiguration {

    @Bean
    public static MRepositoriesRegistrar mRepositoriesRegistrar(
            ObjectProvider<List<MRepositoryMethodInvoker<?>>> methodInvokersProvider,
            Environment environment
    ) {
        return new MRepositoriesRegistrar(
                methodInvokersProvider,
                environment
        );
    }
}
