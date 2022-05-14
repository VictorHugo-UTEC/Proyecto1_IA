package AgentesIA;

import jadex.base.PlatformConfiguration;
import jadex.base.Starter;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IExternalAccess;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.cms.CreationInfo;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.SUtil;
import jadex.commons.future.IFuture;

public class Ejecutar {

	public static void main(String[] args) {
		
		
		// Aca se lanza la plataforma de Jadex
		PlatformConfiguration config = PlatformConfiguration.getDefaultNoGui();
					
		IExternalAccess platform = Starter.createPlatform(config).get();
		IFuture<IComponentManagementService> fut = SServiceProvider.getService(platform, IComponentManagementService.class);
		IComponentManagementService cms = fut.get();
		IComponentIdentifier cid = cms.createComponent("myAgent1", "AgentesIA.AgenteBatalla1BDI.class", null).getFirstResult();
		System.out.println("Componente creado id: " + cid);
		CreationInfo ci = new CreationInfo(SUtil.createHashMap(new String[]{"receiver"}, new Object[]{cid}));
		IComponentIdentifier cid2 = cms.createComponent("myAgent2", "AgentesIA.AgenteBatalla2BDI.class", ci).getFirstResult();
		System.out.println("Componente creado id: " + cid2);
						
	}

}
