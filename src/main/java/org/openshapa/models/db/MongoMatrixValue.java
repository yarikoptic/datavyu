/*
 * Copyright (c) 2011 OpenSHAPA Foundation, http://openshapa.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.openshapa.models.db;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;


public final class MongoMatrixValue extends MongoValue implements MatrixValue {
    
    public MongoMatrixValue() {}
    
    /**
     * Default constructor for serializing/deserializing with Mongo
     */
    public MongoMatrixValue(ObjectId parent_id, Argument type) {
        this.put("parent_id", parent_id);
        
        this.save();
        
        for(Argument arg : type.childArguments) {
            createArgument(Argument.Type.NOMINAL);
        }
        
    }

    @Override
    public void set(final String value) {
        this.put("value", value);
        this.save();
    }
    
    @Override
    public String toString() {
        String result = "";
        List<Value> values = getArguments();
        for(int i = 0; i < values.size(); i++) {
            Value v = values.get(i);
            if (v.toString() == null) {
                result += "<arg"+String.valueOf(i)+">";
            } else {
                result += v.toString();
            }
            if (i < values.size() - 1) {
                result += ",";
            }
        }

        return result;
    }

    @Override
    public List<Value> getArguments() {
        List<Value> values = new ArrayList<Value>();
        BasicDBObject query = new BasicDBObject();
        query.put("parent_id", this.get("_id"));
        
        
        DBCursor cur = MongoDatastore.getDB().getCollection("matrix_values").find(query);
        while(cur.hasNext()) {
            values.add( (MongoMatrixValue)cur.next() );
        }

        cur = MongoDatastore.getDB().getCollection("nominal_values").find(query);
        while(cur.hasNext()) {
            values.add( (MongoNominalValue)cur.next() );
        }

        cur = MongoDatastore.getDB().getCollection("text_values").find(query);
        while(cur.hasNext()) {
            values.add( (MongoTextValue)cur.next() );
        }
        
        System.out.println("Got " + String.valueOf(values.size()) + " values belonging to matrix");
        
        return values;
    }
    
    @Override
    public void save() {
        MongoDatastore.getDB().getCollection("matrix_values").save(this);
    }

    @Override
    public Value createArgument(Argument.Type argType) {
        Value val = null;
        String name = "arg" + Integer.toString(getArguments().size());
        if(argType == Argument.Type.NOMINAL) {
            val = new MongoNominalValue((ObjectId)this.get("_id"), name);
        } else if(argType == Argument.Type.TEXT) {
            val = new MongoTextValue((ObjectId)this.get("_id"), name);
        }
        this.save();
        return val;
    }
}
