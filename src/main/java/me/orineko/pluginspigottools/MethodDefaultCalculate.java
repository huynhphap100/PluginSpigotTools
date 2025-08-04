package me.orineko.pluginspigottools;

import net.objecthunter.exp4j.ExpressionBuilder;

import javax.annotation.Nonnull;

public class MethodDefaultCalculate {
    /**
     * Calculate the result of a mathematical expression in string form.
     *
     * @param text the mathematical expression.
     * @return the result as a double.
     */
    public static double stringCalculate(@Nonnull String text) {
        return new ExpressionBuilder(text).build().evaluate();
    }
}
