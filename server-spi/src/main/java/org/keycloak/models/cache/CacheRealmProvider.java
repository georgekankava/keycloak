package org.keycloak.models.cache;

import org.keycloak.models.RealmProvider;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface CacheRealmProvider extends RealmProvider {
    void clear();
    RealmProvider getDelegate();

    void registerRealmInvalidation(String id);

    void registerApplicationInvalidation(String id);
    void registerClientTemplateInvalidation(String id);

    void registerRoleInvalidation(String id);

    void registerGroupInvalidation(String id);
}
