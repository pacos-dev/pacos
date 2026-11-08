delete from PACOS_CREDENTIAL_ACCESS
where permission = 'VIEW'
  and exists (
      select 1
      from PACOS_CREDENTIAL_ACCESS use_grant
      where use_grant.credential_id = PACOS_CREDENTIAL_ACCESS.credential_id
        and use_grant.principal_type = PACOS_CREDENTIAL_ACCESS.principal_type
        and use_grant.principal_id = PACOS_CREDENTIAL_ACCESS.principal_id
        and use_grant.permission = 'USE'
  );

update PACOS_CREDENTIAL_ACCESS
set permission = 'ACCESS'
where permission in ('VIEW', 'USE');
