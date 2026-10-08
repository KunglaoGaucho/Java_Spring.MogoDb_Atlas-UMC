package dev.andrenovaes.sentinela.identidade.validacao;

import java.util.Objects;

import org.springframework.beans.BeanWrapperImpl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CamposIguaisValidator implements ConstraintValidator<CamposIguais, Object> {

    private String campo;
    private String confirmacao;

    @Override
    public void initialize(CamposIguais anotacao) {
        this.campo = anotacao.campo();
        this.confirmacao = anotacao.confirmacao();
    }

    @Override
    public boolean isValid(Object alvo, ConstraintValidatorContext contexto) {
        if (alvo == null) {
            return true;
        }
        var wrapper = new BeanWrapperImpl(alvo);
        Object valor = wrapper.getPropertyValue(campo);
        Object valorConfirmacao = wrapper.getPropertyValue(confirmacao);
        if (Objects.equals(valor, valorConfirmacao)) {
            return true;
        }
        contexto.disableDefaultConstraintViolation();
        contexto.buildConstraintViolationWithTemplate(contexto.getDefaultConstraintMessageTemplate())
                .addPropertyNode(confirmacao)
                .addConstraintViolation();
        return false;
    }
}
