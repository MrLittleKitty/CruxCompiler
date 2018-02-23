package types;

public class BoolType extends Type {

    public BoolType() {
    }

    @Override
    public String toString() {
        return "bool";
    }

    public Type and(Type that) {
        if (!(that instanceof BoolType))
            return super.and(that);

        return new BoolType();
    }

    public Type or(Type that) {
        if (!(that instanceof BoolType))
            return super.or(that);

        return new BoolType();
    }

    public Type not() {
        return new BoolType();
    }

    public Type assign(Type type) {
        if (type instanceof BoolType)
            return new BoolType();

        return super.assign(type);
    }

    @Override
    public boolean equivalent(Type that) {
        if (that == null)
            return false;
        if (!(that instanceof BoolType))
            return false;

        return true;
    }
}    
