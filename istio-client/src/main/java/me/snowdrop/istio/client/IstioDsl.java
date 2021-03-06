package me.snowdrop.istio.client;

import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import me.snowdrop.istio.api.networking.v1alpha3.DoneableEnvoyFilter;
import me.snowdrop.istio.api.networking.v1alpha3.EnvoyFilter;
import me.snowdrop.istio.api.networking.v1alpha3.EnvoyFilterList;
import me.snowdrop.istio.api.networking.v1beta1.*;
import me.snowdrop.istio.api.rbac.v1alpha1.*;

public interface IstioDsl {
	MixedOperation<me.snowdrop.istio.api.networking.v1alpha3.DestinationRule, me.snowdrop.istio.api.networking.v1alpha3.DestinationRuleList, me.snowdrop.istio.api.networking.v1alpha3.DoneableDestinationRule, Resource<me.snowdrop.istio.api.networking.v1alpha3.DestinationRule, me.snowdrop.istio.api.networking.v1alpha3.DoneableDestinationRule>> v1alpha3DestinationRule();

	MixedOperation<DestinationRule, DestinationRuleList, DoneableDestinationRule, Resource<DestinationRule, DoneableDestinationRule>> v1beta1DestinationRule();

	MixedOperation<EnvoyFilter, EnvoyFilterList, DoneableEnvoyFilter, Resource<EnvoyFilter, DoneableEnvoyFilter>> v1alpha3EnvoyFilter();

	MixedOperation<Gateway, GatewayList, DoneableGateway, Resource<Gateway, DoneableGateway>> v1beta1Gateway();

	MixedOperation<me.snowdrop.istio.api.networking.v1alpha3.Gateway, me.snowdrop.istio.api.networking.v1alpha3.GatewayList, me.snowdrop.istio.api.networking.v1alpha3.DoneableGateway, Resource<me.snowdrop.istio.api.networking.v1alpha3.Gateway, me.snowdrop.istio.api.networking.v1alpha3.DoneableGateway>> v1alpha3Gateway();

	MixedOperation<ServiceEntry, ServiceEntryList, DoneableServiceEntry, Resource<ServiceEntry, DoneableServiceEntry>> v1beta1ServiceEntry();

	MixedOperation<me.snowdrop.istio.api.networking.v1alpha3.ServiceEntry, me.snowdrop.istio.api.networking.v1alpha3.ServiceEntryList, me.snowdrop.istio.api.networking.v1alpha3.DoneableServiceEntry, Resource<me.snowdrop.istio.api.networking.v1alpha3.ServiceEntry, me.snowdrop.istio.api.networking.v1alpha3.DoneableServiceEntry>> v1alpha3ServiceEntry();

	MixedOperation<VirtualService, VirtualServiceList, DoneableVirtualService, Resource<VirtualService, DoneableVirtualService>> v1beta1VirtualService();

	MixedOperation<me.snowdrop.istio.api.networking.v1alpha3.VirtualService, me.snowdrop.istio.api.networking.v1alpha3.VirtualServiceList, me.snowdrop.istio.api.networking.v1alpha3.DoneableVirtualService, Resource<me.snowdrop.istio.api.networking.v1alpha3.VirtualService, me.snowdrop.istio.api.networking.v1alpha3.DoneableVirtualService>> v1alpha3VirtualService();

	MixedOperation<ServiceRoleBinding, ServiceRoleBindingList, DoneableServiceRoleBinding, Resource<ServiceRoleBinding, DoneableServiceRoleBinding>> v1alpha1ServiceRoleBinding();

	MixedOperation<ServiceRole, ServiceRoleList, DoneableServiceRole, Resource<ServiceRole, DoneableServiceRole>> v1alpha1ServiceRole();
}
