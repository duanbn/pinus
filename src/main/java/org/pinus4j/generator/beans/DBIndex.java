/**
 * Copyright 2014 Duan Bingnan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pinus4j.generator.beans;

import java.io.Serializable;

import org.pinus4j.utils.StringUtils;

/**
 * 数据库索引bean.
 * 抽象一个数据库索引
 *
 * @author duanbn
 */
public class DBIndex implements Serializable {

    /**
     * 被索引的字段名.
     */
    private String field;

    /**
     * 是否是唯一索引
     */
    private boolean isUnique;

    /**
     * 生成索引名
     */
    public String getIndexName() {
        StringBuilder indexName = new StringBuilder();
        indexName.append("index__").append(StringUtils.removeBlank(field).replaceAll(",", "__"));
        return indexName.toString();
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public boolean isUnique() {
        return isUnique;
    }

    public void setUnique(boolean isUnique) {
        this.isUnique = isUnique;
    }

    @Override
	public String toString() {
		return "DBIndex [field=" + field + ", isUnique=" + isUnique + "]";
	}

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((field == null) ? 0 : field.hashCode());
        result = prime * result + (isUnique ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DBIndex other = (DBIndex) obj;
        if (field == null) {
            if (other.field != null)
                return false;
        } else if (!field.equals(other.field))
            return false;
        if (isUnique != other.isUnique)
            return false;
        return true;
    }

}
