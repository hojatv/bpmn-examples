package org.bpmnwithactiviti.chapter5;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ingrian.internal.xml.FatalErrorFromServerException;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricFormProperty;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.bpmnwithactiviti.common.AbstractTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:chapter4/spring-nodeployment-application-context.xml")
public class FormServiceTest extends AbstractTest {
	
	/*@Rule
	public ActivitiRule activitiRule = new ActivitiRule("activiti.cfg-mem.xml");*/
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private HistoryService historyService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private FormService formService;
	@Autowired
	@Rule
	public ActivitiRule activitiRule;

	@Test
	/*@Deployment(resources={"chapter5/startform.bpmn20.xml"})*/
	public void startFormSubmit() {
		ProcessDefinition definition = activitiRule.getRepositoryService()
				.createProcessDefinitionQuery().processDefinitionKey("startFormTest").list().get(0);
		/*ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("startFormTest").singleResult();
		assertNotNull(definition);*/
		/*FormService formService = activitiRule.getFormService();*/
		List<FormProperty> formList = formService.getStartFormData(definition.getId()).getFormProperties();
		assertEquals(4, formList.size());
		Map<String, String> formProperties = new HashMap<String, String>();
		formProperties.put("name", "Miss Piggy");
		formProperties.put("emailAddress", "piggy@localhost");
		formProperties.put("income", "400");
		formProperties.put("loanAmount", "100");
		
		formService.submitStartFormData(definition.getId(), formProperties);
		/*List<HistoricDetail> historyVariables = activitiRule.getHistoryService()
        	.createHistoricDetailQuery()
        	.formProperties()
        	.list();*/
		List<HistoricDetail> historyVariables = historyService
				.createHistoricDetailQuery()
				.formProperties()
				.list();

		assertNotNull(historyVariables);
		/*assertEquals(4, historyVariables.size());*/
		HistoricFormProperty formProperty = (HistoricFormProperty) historyVariables.get(0);
		assertEquals("loanAmount", formProperty.getPropertyId());
		assertEquals("100", formProperty.getPropertyValue());
		
		formProperty = (HistoricFormProperty) historyVariables.get(1);
		assertEquals("income", formProperty.getPropertyId());
		assertEquals("400", formProperty.getPropertyValue());
	}

}
