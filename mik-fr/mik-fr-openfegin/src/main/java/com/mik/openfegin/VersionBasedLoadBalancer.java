package com.mik.openfegin;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class VersionBasedLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private final String serviceId;
    private final Random random;

    public VersionBasedLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
                                    String serviceId) {
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.serviceId = serviceId;
        this.random = new Random();
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider.getIfAvailable();
        return Objects.requireNonNull(supplier).get().next().map(serviceInstances -> getInstanceResponse(serviceInstances, request));
    }

    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances, Request request) {

        if (instances.isEmpty()) {
            return new EmptyResponse();
        }

        String version;
        if (request instanceof DefaultRequest) {
            DefaultRequest<?> defaultRequest = (DefaultRequest<?>) request;
            if (defaultRequest.getContext() instanceof RequestDataContext context) {
                version = context.getClientRequest().getHeaders().getFirst("X-Version");
            } else {
                version = null;
            }
        } else {
            version = null;
        }

        ServiceInstance serviceInstance;

        if (version == null) {
            serviceInstance = instances.get(random.nextInt(instances.size()));
        }else {
            List<ServiceInstance> filteredInstances = instances.stream()
                    .filter(instance -> version.equals(instance.getMetadata().get("version")))
                    .toList();
            if (filteredInstances.isEmpty()) {
                return new EmptyResponse();
            }
            serviceInstance = filteredInstances.get(random.nextInt(filteredInstances.size()));
        }

        return new DefaultResponse(serviceInstance);
    }
}

