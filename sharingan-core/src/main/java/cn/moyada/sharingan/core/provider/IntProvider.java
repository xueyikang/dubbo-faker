package cn.moyada.sharingan.core.provider;

import cn.moyada.sharingan.core.support.IntRange;

/**
 * 随机数值提供器
 * @author xueyikang
 * @since 1.0
 **/
public class IntProvider extends RandomProvider implements ArgsProvider {

    private final boolean huge;

    private final int base;

    private final int range;

    public IntProvider(String value, Class<?> paramType, IntRange intRange) {
        super(value, paramType, intRange.getTarget());

        int top = intRange.getEnd() - intRange.getStart();
        boolean huge = intRange.getEnd() >= 0;
        huge = huge && top < 0;

        if (huge) {
            this.range = intRange.getEnd();
            this.base = intRange.getStart();
            this.huge = true;
        }
        else {
            this.range = top;
            this.base = intRange.getStart();
            this.huge = false;
        }
    }

    @Override
    protected String next() {
        int data;
        if (huge) {
            data = random.nextInt(range) - random.nextInt(base);
        } else {
            data = random.nextInt(range) + base;
        }
        return Integer.toString(data);
    }

//    @Override
//    public Object fetchNext() {
//        int data;
//        if (huge) {
//            data = random.nextInt(range) + base;
//        } else {
//            data = random.nextInt(range) - random.nextInt(base);
//        }
//        String newData = value.replace(target, Integer.toString(data));
//        Object next = convert(newData, paramType);
//        return next;
//    }

    public static void main(String[] args) {
        System.out.println(Integer.MIN_VALUE +245);
        System.out.println((Integer.MIN_VALUE + 245) + Integer.MIN_VALUE);
    }
}