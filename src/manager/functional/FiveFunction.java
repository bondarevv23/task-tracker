package manager.functional;

@FunctionalInterface
public interface FiveFunction <A, B, C, D, E, F> {
    F apply(A a, B b, C c, D d, E e);
}
