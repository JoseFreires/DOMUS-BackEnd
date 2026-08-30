package com.domus.tcc.backend.domain;

import com.domus.tcc.backend.dto.request.DadosRegistrarPessoaAutorizadaDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor // Adicione isso
@AllArgsConstructor // Adicione isso
@Table(name = "pessoa_autorizada")
@Entity (name = "PessoaAutorizada")
@EqualsAndHashCode (of = "idPessoaAutorizada")
@Getter
@Setter
public class PessoaAutorizada {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "idPesssoaAutorizada") //Bah está com 3 S, coisa boa, vou deixar assim.
        private Long idPessoaAutorizada;
        
        @Column(name = "nome")
        private String nome;
        
        @Column(unique = true)
        private String cpf;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "morador_idMorador", nullable = false)
        private Morador morador;

        @Column(name = "ativo")
        private Boolean ativo;


        public PessoaAutorizada(DadosRegistrarPessoaAutorizadaDTO dados, Morador morador) {
                this.nome = dados.nome();
                this.cpf = dados.cpf();
                this.morador = morador;
                this.ativo = true;
        }
}

