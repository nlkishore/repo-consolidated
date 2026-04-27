package com.mycompany.webapp.modules.actions;



import java.util.List;

import org.apache.torque.criteria.Criteria;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

import com.mycompany.webapp.om.Author;
import com.mycompany.webapp.om.AuthorPeer;

public class ShowRecords  extends SecureAction 
{

	@Override
	public void doPerform( PipelineData pipelineData, Context context )
	    throws Exception
	{
	    super.doPerform( pipelineData, context );
	    List<Author> authors = AuthorPeer.doSelect( new Criteria() );//all
	    context.put( "authors", authors );
	    RunData data = (RunData) pipelineData;
	    data.setScreenTemplate(Character.toLowerCase(getClass().getSimpleName().charAt(0)) + 
	    		getClass().getSimpleName().substring(1) + ".vm"
	    ); 
	    
	}
}
