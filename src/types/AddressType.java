package types;

public class AddressType extends Type {

    private Type base;

    public AddressType(Type base) {
        this.base = base;
    }

    public Type base() {
        return base;
    }

    public Type deref() {
        return base;
    }

    public Type index(Type type) {
        if (type instanceof IntType && base instanceof ArrayType)
            return new AddressType(base.index(type));
        return super.index(type);
    }

    public Type assign(Type type) {
        if (type.equivalent(base))
            return type.assign(base);

        return super.assign(type);
    }

    @Override
    public String toString() {
        return "Address(" + base + ")";
    }

    @Override
    public boolean equivalent(Type that) {
        if (that == null)
            return false;
        if (!(that instanceof AddressType))
            return false;

        AddressType aType = (AddressType) that;
        return this.base.equivalent(aType.base);
    }
}
