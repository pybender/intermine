package org.intermine.dataconversion;

/*
 * Copyright (C) 2002-2004 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.intermine.model.fulldata.Item;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.SingletonResults;

import org.apache.log4j.Logger;

/**
 * Provides an interface between a DataTranslator and the source Item ObjectStore which it wishes to
 * read.
 *
 * @author Matthew Wakeling
 * @author Richard Smith
 */
public class ObjectStoreItemReader implements ItemReader
{
    private ObjectStoreItemPathFollowingImpl os;

    /**
     * Constructs a new ObjectStoreItemReader.
     *
     * @param os the ObjectStore
     */
    public ObjectStoreItemReader(ObjectStore os) {
        this.os = new ObjectStoreItemPathFollowingImpl(os);
    }

    /**
     * Constructs a new ObjectStoreItemReader with the given path info.
     *
     * @param os the ObjectStore
     * @param paths the paths
     */
    public ObjectStoreItemReader(ObjectStore os, Map paths) {
        this.os = new ObjectStoreItemPathFollowingImpl(os, paths);
    }

    /**
     * @see ItemReader#itemIterator
     */
    public Iterator itemIterator() throws ObjectStoreException {
        Query q = new Query();
        // database has a hard time selecting distinct on object xml
        q.setDistinct(false);
        QueryClass qc = new QueryClass(Item.class);
        q.addFrom(qc);
        q.addToSelect(qc);
        SingletonResults sr = new SingletonResults(q, os, os.getSequence());
        sr.setBatchSize(1000);
        return sr.iterator();
    }

    /**
     * @see ItemReader#getItemById
     */
    public Item getItemById(String objectId) throws ObjectStoreException {
        List results = getItemsByDescription(Collections.singleton(
                    new FieldNameAndValue("identifier", objectId, false)));
        if (results.size() > 1) {
            throw new IllegalStateException("Multiple Items in the objectstore with identifier "
                    + objectId + ", size = " + results.size()
                    + (results instanceof SingletonResults ? "query = " + ((SingletonResults)
                            results).getQuery() : ""));
        } else if (results.size() == 1) {
            return (Item) results.get(0);
        }
        return null;
    }

    /**
     * @see ItemReader#getItemsByDescription
     */
    public List getItemsByDescription(Set constraints) throws ObjectStoreException {
        return os.getItemsByDescription(constraints);
    }
}
