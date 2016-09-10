package sne.geotracker;

/**
 * This class couples together a pair of values, which may be of different
 * types (T and U). The individual values can be accessed through the
 * accessors.
 */
public class Pair<T, U>
{
    private T _first;
    private U _second;

    /**
     * Constructs a Pair object with its members '_first' and '_second'
     * initialized to null.
     */
    public Pair()
    {
        _first = null;
        _second = null;
    }

    /**
     * Constructs a Pair object with its members '_first' and '_second'
     * initialized to 'first' and 'second', respectively.
     * @param first
     * @param second
     */
    public Pair(T first, U second)
    {
        _first = first;
        _second = second;
    }

    public void setFirst(T first)
    {
        this._first = first;
    }

    public T getFirst()
    {
        return _first;
    }

    public void setSecond(U second)
    {
        this._second = second;
    }

    public U getSecond()
    {
        return _second;
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other) return true;
        if (other == null) return false;
        if (getFirst() == null) return false;
        if (getSecond() == null) return false;
        if (getClass() != other.getClass()) return false;

        Pair<?, ?> p = (Pair<?, ?>) other;

        return getFirst().equals(p.getFirst())
                && getSecond().equals(p.getSecond());
    }

    @Override
    public int hashCode()
    {
        if (_first == null && _second == null) return super.hashCode();
        if (_first != null && _second == null) return _first.hashCode();
        if (_first == null && _second != null) return _second.hashCode();
        return (_first.hashCode() ^ _second.hashCode());
    }

    @Override
    public String toString()
    {
        String f;
        if (getFirst() == null) f = "null";
        else                    f = getFirst().toString();

        String s;
        if (getSecond() == null) s = "null";
        else                     s = getSecond().toString();

        return "("+f+", "+s+")";
    }
}
