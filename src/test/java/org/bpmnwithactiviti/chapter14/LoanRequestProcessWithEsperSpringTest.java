package org.bpmnwithactiviti.chapter14;

import com.espertech.esper.client.*;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.bpmnwithactiviti.common.AbstractTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:chapter4/spring-nodeployment-application-context.xml")
public class LoanRequestProcessWithEsperSpringTest extends AbstractTest {
	
	/*@Rule
	public ActivitiRule activitiRule = new ActivitiRule("activiti.cfg-mem-fullhistory.xml");*/
	/*@Autowired
	ActivitiRule activitiRule;*/
	@Autowired
	RuntimeService runtimeService;
	@Autowired
	TaskService taskService;
	@Autowired
	RepositoryService repositoryService;
	private EPAdministrator epAdmin;
	
	@Before
	public void startEsper() {
		Configuration configuration = new Configuration();
		configuration.addEventTypeAutoName("org.bpmnwithactiviti.chapter14.bam.event");
		EPServiceProvider epService = EPServiceProviderManager.getDefaultProvider(configuration);
		epAdmin = epService.getEPAdministrator();
		
		epAdmin.createEPL( 
				"select avg(endEvent.processedTime - beginEvent.receiveTime) as avgProcessDuration" +
				" from pattern [" +
				"	every beginEvent=LoanRequestReceivedEvent" +
				"   -> endEvent=LoanRequestProcessedEvent(processInstanceId=beginEvent.processInstanceId)" +
				" ].win:length(20)", "processDuration");
	}
	
	private Double avgProcessDuration = null;

	@Test
	@Deployment(resources={"chapter14/loanrequest_withespertest.bpmn20.xml"})
	public void testEsperActivitiSetup() throws Exception {
		/*RuntimeService runtimeService = activitiRule.getRuntimeService();
		TaskService taskService = activitiRule.getTaskService();*/
		
		EPStatement epStatement = epAdmin.getStatement("processDuration");
	  epStatement.addListener(new UpdateListener () {
	    public void update(EventBean[] newEvents, EventBean[] oldEvents) {
	      avgProcessDuration = (Double) newEvents[0].get("avgProcessDuration");
	    }
	  });
		repositoryService.createDeployment()
				.addClasspathResource("chapter14/loanrequest_withespertest.bpmn20.xml").deploy();

		List deployments = repositoryService.createDeploymentQuery().list();
		
		// Start first loan request
	  Map<String, Object> processVariables = new HashMap<String, Object>();
		processVariables.put("loanAmount", 10);
		runtimeService.startProcessInstanceByKey("loanrequest_withespertest", processVariables);
		
		Thread.sleep(1000);

		// Evaluate first loan request
		processVariables = new HashMap<String, Object>();
		processVariables.put("requestApproved", true);
		Task result = taskService.createTaskQuery().processDefinitionKey("loanrequest_withespertest").list().get(0);
		taskService.complete(result.getId(), processVariables);
		
		System.out.println("<<< avgProcessDuration = " + avgProcessDuration);
		assertTrue(avgProcessDuration >= 1000);

		// Start second loan request
		processVariables = new HashMap<String, Object>();
		processVariables.put("loanAmount", 20);
		runtimeService.startProcessInstanceByKey("loanrequest_withespertest", processVariables);
		
		Thread.sleep(2000);

		// Evaluate second loan request
		processVariables = new HashMap<String, Object>();
		processVariables.put("requestApproved", true);
		taskService.complete(taskService.createTaskQuery().singleResult().getId(), processVariables);
		
		System.out.println("<<< avgProcessDuration = " + avgProcessDuration);
		assertTrue(avgProcessDuration >= 1500);
	}
}
