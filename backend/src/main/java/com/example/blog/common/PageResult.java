package com.example.blog.common;

import java.io.Serializable;
import java.util.List;

public record PageResult<T>(long total, long page, long size, List<T> records) implements Serializable {
}
