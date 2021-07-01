package com.bilicraft.whocraftit;

import com.google.gson.Gson;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

public class DataContainerType implements PersistentDataType<String, DataContainer> {
    static final DataContainerType INSTANCE = new DataContainerType();
    static final Gson GSON = new Gson();
    /**
     * Returns the primitive data type of this tag.
     *
     * @return the class
     */
    @Override
    public Class<String> getPrimitiveType() {
        return String.class;
    }

    /**
     * Returns the complex object type the primitive value resembles.
     *
     * @return the class type
     */
    @Override
    public Class<DataContainer> getComplexType() {
        return DataContainer.class;
    }

    /**
     * Returns the primitive data that resembles the complex object passed to
     * this method.
     *
     * @param complex the complex object instance
     * @param context the context this operation is running in
     * @return the primitive value
     */
    @Override
    public String toPrimitive(DataContainer complex, PersistentDataAdapterContext context) {
        return GSON.toJson(complex);
    }

    /**
     * Creates a complex object based of the passed primitive value
     *
     * @param primitive the primitive value
     * @param context   the context this operation is running in
     * @return the complex object instance
     */
    @Override
    public DataContainer fromPrimitive(String primitive, PersistentDataAdapterContext context) {
        return GSON.fromJson(primitive,DataContainer.class);
    }
}
