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

import java.util.Date;
import org.cdlib.mrt.core.DateState;


import org.cdlib.mrt.inv.utility.InvUtil;
import org.cdlib.mrt.utility.StringUtil;

/**
 * OAI Date
 * @author dloy
 */
public class SwordDate
{

    protected static final String NAME = "OAIDate";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = true;
    public enum Type {mrt, sword, db }
    protected String in = null;
    protected DateState dateState = null;
    
    
    public static SwordDate getMrtDate(String dateS)
    {
        if (dateS == null) return null;
        int periodPos = dateS.indexOf('.');
        if (periodPos >= 0) {
            dateS = dateS.substring(0, periodPos);
        }
        
        return new SwordDate(Type.mrt, dateS);
    }
    
    public static SwordDate getOAIDate(String dateS)
    {
        return new SwordDate(Type.sword, dateS);
    }
    
    public static SwordDate getDBDate(String dateS)
    {
        return new SwordDate(Type.db, dateS);
    }
    
    public static SwordDate getUnixDate(Date inDate)
    {
        DateState dateState = new DateState();
        dateState.setDate(inDate);
        return new SwordDate(dateState);
    }
    
    protected SwordDate(Type type, String dateS)
    {
        this.in = dateS;
        if (StringUtil.isAllBlank(dateS)) {
            throw new RuntimeException("Empty data");
        }
        switch (type) {
            case mrt:
                inMrt();
                break;
                    
            case sword:
                inSword();
                break;
                         
            case db: 
                inDB();
                break;
                        
            default:
                System.out.println("Unknown Date type");
                break;
        }
    }
    
    public static SwordDate getSwordDate(DateState state)
    {
        return new SwordDate(state);
    }
    
    protected SwordDate(DateState state)
    {
        dateState = state;
    }
        
    protected void inMrt()
    {
        this.dateState = new DateState(in);
    }
    
    protected void inDB()
    {
        this.dateState = InvUtil.setDBDate(in);
    }
    
    protected void inSword()
    {
        this.dateState = InvUtil.setOAIDate(in);
    }
    
    public DateState getDateState()
    {
        return this.dateState;
    }
    
    public String getMrt()
    {
        return dateState.getIsoDate();
    }
    
    public String getDB()
    {
        return InvUtil.getDBDate(dateState);
    }
    
    public String getOAI()
    {
        return InvUtil.getOAIDate(dateState);
    }
    
    public Date getUnixDate()
    {
        if (dateState == null) return null;
        return dateState.getDate();
    }
    
    public void setUnixDate(Date unixDate)
    {
        if (unixDate == null) return;
        DateState dateState = new DateState(unixDate);
        this.dateState = dateState;
    }
    
    public String dump(String header)
    {
        StringBuffer buf = new StringBuffer();
        buf.append("OAIDate:");
        if (header != null) {
            buf.append(header);
        }
        buf.append(" - mrt:" + getMrt());
        buf.append(" - db:" + getDB());
        buf.append(" - oai:" + getOAI());
        return buf.toString();
    }
}

