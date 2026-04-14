package com.auth.demo.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity( name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class UserModel extends PessoaModel implements UserDetails {

    @Column
    @NotNull(message = "A idade é obrigatória")
    @Min(value = 0, message = "A idade não pode ser menor que 0")
    @Max(value = 150, message = "A idade não pode ser maior que 150")
    private int idade;

    @Column
    @Size(min = 2, max = 200, message = "A profissão deve ter entre 2 e 200 caracteres.")
    private String profissao;

    @Column
    private boolean status;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "token_expiration")
    private LocalDateTime tokenExpiration;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<PagamentoModel> pagamentos;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        return getEmail();
    }

    @Override
    public String getPassword() {
        return getSenha();
    }
    
}
