package com.epam.ld.cache_service;

import java.util.Objects;

public class Entity {
    private String str;

    public Entity(){}

    public Entity(String str) {
        this.str = str;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return str.equals(entity.str);
    }

    @Override
    public int hashCode() {
        return Objects.hash(str);
    }
}
