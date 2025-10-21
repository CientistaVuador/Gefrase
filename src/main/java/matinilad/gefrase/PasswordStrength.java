/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <https://unlicense.org>
 */
package matinilad.gefrase;

import java.awt.Color;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Enum de força de senha de acordo com a quantidade de bits,
 * se uma senha demora mais de 100 anos para ser quebrada
 * com X tentativas por segundo então é assumida como sendo segura.
 *
 * @author Cien
 */
public enum PasswordStrength {

    /**
     * O nível "Muito Alta" de força, assumindo 10^21 tentativas por segundo<br>
     * <br>
     * Hardware (Estimado): Rede do Bitcoin<br>
     * Contra: SHA256<br>
     */
    VERY_HIGH("Muito Alta", Color.CYAN, calculateBits(1.0E21)),
    /**
     * O nível "Alta" de força, assumindo 10^15 tentativas por segundo<br>
     * <br>
     * Hardware (Estimado): Rede do Bitcoin<br>
     * Contra: PBKDF2 (1.000.000 Iterações)<br>
     */
    HIGH("Alta", Color.GREEN, calculateBits(1.0E15)),
    /**
     * O nível "Média" de força, assumindo 5*(10^10) tentativas por segundo<br>
     * <br>
     * Hardware (Estimado): 2x RTX5090<br>
     * Contra: SHA256<br>
     */
    MEDIUM("Média", Color.YELLOW, calculateBits(5.0E10)),
    /**
     * O nível "Baixa" de força, assumindo 50.000 tentativas por segundo<br>
     * <br>
     * Hardware (Estimado): 2x RTX5090<br>
     * Contra: PBKDF2 (1.000.000 Iterações)<br>
     */
    LOW("Baixa", Color.PINK, calculateBits(50000.0)),
    /**
     * O nível "Muito Baixa" de força, assumindo 1 tentativa por segundo<br>
     * <br>
     * Hardware (Estimado): Ataque online<br>
     * Contra: Website limitando a uma tentativa por segundo por conta<br>
     */
    VERY_LOW("Muito Baixa", Color.RED, calculateBits(1.0)),
    /**
     * O nível "INSEGURA" de força, qualquer senha com este nível não deve ser usada.
     */
    INSECURE("INSEGURA", Color.BLACK, 0f);

    //os campos do enum ordenados de mais bits para menos bits
    //para evitar erros com a ordem do values()
    private static final PasswordStrength[] SORTED;

    static {
        PasswordStrength[] array = values();
        Comparator<PasswordStrength> comparator = (o1, o2) -> {
            return Float.compare(o1.getBits(), o2.getBits());
        };
        Arrays.sort(array, comparator.reversed());

        SORTED = array;
    }

    /**
     * Retorna a força de uma senha de acordo com a quantidade de bits dela
     *
     * @param bits a quantidade de bits da senha
     * @return a força da senha
     */
    public static PasswordStrength from(float bits) {
        for (PasswordStrength p : SORTED) {
            if (bits >= p.getBits()) {
                return p;
            }
        }
        return PasswordStrength.INSECURE;
    }

    /**
     * Calcula os bits necessários para que um ataque de força bruta demore 100 anos de acordo com as tentativas por segundo.
     *
     * @param attemptsPerSecond as tentativas por segundo
     * @return os bits necessários para 100 anos de tempo
     */
    public static float calculateBits(double attemptsPerSecond) {
        //assumindo 100 anos para se quebrar a senha
        //60 segundos * 60 minutos * 24 horas * 365 dias * 100 anos = 3.153.600.000 tentativas
        double attempts = attemptsPerSecond * 3153600000.0;
        double bits = Math.log(attempts) / Math.log(2.0);
        return (float) bits;
    }

    //o nome do enum localizado
    private final String localizedName;
    //a cor que simboliza esse enum
    private final Color color;
    //os bits do enum
    private final float bits;

    private PasswordStrength(String localizedName, Color color, float bits) {
        this.localizedName = localizedName;
        this.color = color;
        this.bits = bits;
    }

    /**
     * Retorna o nome da força da senha localizada (ex: VERY_HIGH -> Muito Alta)
     *
     * @return o nome localizado
     */
    public String getLocalizedName() {
        return localizedName;
    }

    /**
     * Retorna a cor que simboliza essa força de senha
     *
     * @return a cor que simboliza essa força de senha
     */
    public Color getColor() {
        return color;
    }

    /**
     * Retorna a quantidade de bits dessa força de senha
     *
     * @return a quantidade de bits dessa força de senha
     */
    public float getBits() {
        return bits;
    }

}
