/******************************************************************************
Copyright (c) 2005-2012, Regents of the University of California
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:
 *
- Redistributions of source code must retain the above copyright notice,
  this list of conditions and the following disclaimer.
- Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.
- Neither the name of the University of California nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.
*******************************************************************************/
package org.swordapp.server.element;



import org.cdlib.mrt.inv.content.InvCollection;

/**
 * Run SWORD RecordAction
 * @author dloy
 */
public class SwordCollection
{

    protected static final String NAME = "SwordCollection";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = true;
    public final static String COLLECTION ="/sword/v2/collection/";
    protected String mnemonic = null;
    protected String name = null;
    
    
    public static SwordCollection getOAISet(InvCollection collection)
    {
        return new SwordCollection(collection);
    }
    
    public static SwordCollection getOAISet(String mnemonic, String name)
    {
        return new SwordCollection(mnemonic, name);
    }
    
    protected SwordCollection(InvCollection collection)
    {
        this.mnemonic = collection.getMnemonic();
        this.name = collection.getName();
    }
    
    protected SwordCollection(String mnemonic, String name)
    {
        this.mnemonic = mnemonic;
        this.name = name;
    }
        
    public String getSWORDCollection()
    {
        return COLLECTION + mnemonic;
    }
        
    public static String getSWORDCollection(String remoteMnemonic)
    {
        return COLLECTION + remoteMnemonic;
    }
    
    public static String getMnemonic(String swordCollection)
    {
        if (swordCollection.startsWith(COLLECTION)) {
            return swordCollection.substring(COLLECTION.length());
        }
        return null;
    }
    
    public String getCollectionMnemonic()
    {
        return mnemonic;
    }  
    
    public String getCollectionName()
    {
        return name;
    }  
    
    
    public String dump(String header)
    {
        StringBuffer buf = new StringBuffer();
        buf.append("OAISet:");
        if (header != null) {
            buf.append(header);
        }
        buf.append(" - spec:" + getCollectionMnemonic());
        buf.append(" - name:" + getCollectionName());
        return buf.toString();
    }
}

