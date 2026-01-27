package org.pacos.core.component.security.service;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.pacos.base.exception.PacosException;
import org.pacos.core.component.security.domain.Role;
import org.pacos.core.component.security.dto.RoleDTO;
import org.pacos.core.component.security.dto.RoleMapper;
import org.pacos.core.component.security.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional
    public void createRole(RoleDTO roleDTO) {
        if (roleRepository.existsByLabelAndIdNot(roleDTO.getLabel(), roleDTO.getId())) {
            throw new PacosException("Role with name '" + roleDTO.getLabel() + "' already exists");
        }
        Role role;
        if (roleDTO.getId() == null) {
            role = new Role();
        } else {
            role = roleRepository.findById(roleDTO.getId()).orElse(new Role());
            role.setId(role.getId());
        }
        role.setLabel(roleDTO.getLabel().toUpperCase(Locale.ROOT));
        role.setDescription(role.getDescription());

        roleRepository.save(role);
    }

    public List<RoleDTO> loadAllRoles() {
        return roleRepository.findAllByOrderByLabelAsc().stream().map(RoleMapper::map).toList();
    }

    public int countRoleAssignment(int roleId) {
        return roleRepository.countRoleAssignment(roleId);
    }

    @Transactional
    public void removeRole(RoleDTO roleDTO) {
        roleRepository.findById(roleDTO.getId()).ifPresent(roleRepository::delete);
    }

    @Transactional
    public void cloneRole(RoleDTO roleDTO) {
        roleRepository.findById(roleDTO.getId()).ifPresent(role -> {
            Role copy = new Role();
            copy.setLabel(role.getLabel() + "_COPY_" + roleRepository.count());
            if (copy.getLabel().length() > 254) {
                copy.setLabel(copy.getLabel().substring(0, 254));
            }
            copy.setDescription(roleDTO.getDescription());
            copy.setAppPermissions(new HashSet<>(role.getAppPermissions()));
            roleRepository.save(copy);
        });
    }
}
