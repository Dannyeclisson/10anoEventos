package com.danny.eventos.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfValidator implements ConstraintValidator<CPFValido, String> {

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext context) {
        if (valor == null || valor.isBlank()) {
            return true;
        }

        String cpf = somenteDigitos(valor);
        if (cpf.length() != 11 || todosDigitosIguais(cpf)) {
            return false;
        }

        int primeiroDigito = calcularDigito(cpf.substring(0, 9), 10);
        int segundoDigito = calcularDigito(cpf.substring(0, 10), 11);

        return primeiroDigito == Character.digit(cpf.charAt(9), 10)
                && segundoDigito == Character.digit(cpf.charAt(10), 10);
    }

    public static String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    private boolean todosDigitosIguais(String cpf) {
        return cpf.chars().allMatch(digito -> digito == cpf.charAt(0));
    }

    private int calcularDigito(String numeros, int pesoInicial) {
        int soma = 0;
        for (int indice = 0; indice < numeros.length(); indice++) {
            soma += Character.digit(numeros.charAt(indice), 10) * (pesoInicial - indice);
        }

        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
