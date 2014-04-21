package org.dawnsci.passerelle.cluster.actor.test;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.dawb.passerelle.actors.data.DataChunkSource;
import org.dawb.passerelle.actors.data.DataImportSource;
import org.dawnsci.passerelle.cluster.actor.ClusterNodeTransformer;

import com.isencia.passerelle.domain.et.ETDirector;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.testsupport.actor.DevNullActor;

public class TestClusterActor extends TestCase {

  private FlowManager flowMgr;

  public void setUp() throws Exception {
    flowMgr = new FlowManager();
  }

//  public void test1() throws Exception {
//    Flow flow = new Flow("flow", null);
//    new ETDirector(flow, "director");
//    
//    DataImportSource src = new DataImportSource(flow, "src");
//    ClusterNodeTransformer cnt = new ClusterNodeTransformer(flow, "cnt");
//    
//    flow.connect(src, cnt);
//    
//    src.names.setExpression("/entry1/instrument/Pilatus2M/data");
//    src.path.setExpression("C:/data/workspaces/DAWN/org.dawnsci.passerelle.cluster.actor.test/datafiles/i22-34774.nxs");
//    src.relativePathParam.setExpression("false");
//    src.slicing.setExpression("PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGphdmEgdmVyc2lvbj0iMS43LjBfNTEiIGNsYXNzPSJqYXZhLmJlYW5zLlhNTERlY29kZXIiPgogPG9iamVjdCBjbGFzcz0ib3JnLmRhd25zY2kuc2xpY2luZy5hcGkuc3lzdGVtLkRpbXNEYXRhTGlzdCI+CiAgPHZvaWQgcHJvcGVydHk9ImRpbXNEYXRhIj4KICAgPG9iamVjdCBjbGFzcz0iamF2YS51dGlsLkFycmF5TGlzdCI+CiAgICA8dm9pZCBtZXRob2Q9ImFkZCI+CiAgICAgPG9iamVjdCBjbGFzcz0ib3JnLmRhd25zY2kuc2xpY2luZy5hcGkuc3lzdGVtLkRpbXNEYXRhIj4KICAgICAgPHZvaWQgcHJvcGVydHk9ImRpbWVuc2lvbiI+CiAgICAgICA8aW50PjA8L2ludD4KICAgICAgPC92b2lkPgogICAgICA8dm9pZCBwcm9wZXJ0eT0ic2xpY2VTcGFuIj4KICAgICAgIDxpbnQ+MTwvaW50PgogICAgICA8L3ZvaWQ+CiAgICAgPC9vYmplY3Q+CiAgICA8L3ZvaWQ+CiAgICA8dm9pZCBtZXRob2Q9ImFkZCI+CiAgICAgPG9iamVjdCBjbGFzcz0ib3JnLmRhd25zY2kuc2xpY2luZy5hcGkuc3lzdGVtLkRpbXNEYXRhIiBpZD0iRGltc0RhdGEwIj4KICAgICAgPHZvaWQgY2xhc3M9Im9yZy5kYXduc2NpLnNsaWNpbmcuYXBpLnN5c3RlbS5EaW1zRGF0YSIgbWV0aG9kPSJnZXRGaWVsZCI+CiAgICAgICA8c3RyaW5nPnNsaWNlPC9zdHJpbmc+CiAgICAgICA8dm9pZCBtZXRob2Q9InNldCI+CiAgICAgICAgPG9iamVjdCBpZHJlZj0iRGltc0RhdGEwIi8+CiAgICAgICAgPGludD4yPC9pbnQ+CiAgICAgICA8L3ZvaWQ+CiAgICAgIDwvdm9pZD4KICAgICAgPHZvaWQgcHJvcGVydHk9ImRpbWVuc2lvbiI+CiAgICAgICA8aW50PjE8L2ludD4KICAgICAgPC92b2lkPgogICAgIDwvb2JqZWN0PgogICAgPC92b2lkPgogICAgPHZvaWQgbWV0aG9kPSJhZGQiPgogICAgIDxvYmplY3QgY2xhc3M9Im9yZy5kYXduc2NpLnNsaWNpbmcuYXBpLnN5c3RlbS5EaW1zRGF0YSI+CiAgICAgIDx2b2lkIHByb3BlcnR5PSJkaW1lbnNpb24iPgogICAgICAgPGludD4yPC9pbnQ+CiAgICAgIDwvdm9pZD4KICAgICAgPHZvaWQgcHJvcGVydHk9InBsb3RBeGlzIj4KICAgICAgIDxvYmplY3QgY2xhc3M9ImphdmEubGFuZy5FbnVtIiBtZXRob2Q9InZhbHVlT2YiPgogICAgICAgIDxjbGFzcz5vcmcuZGF3bnNjaS5zbGljaW5nLmFwaS5zeXN0ZW0uQXhpc1R5cGU8L2NsYXNzPgogICAgICAgIDxzdHJpbmc+WDwvc3RyaW5nPgogICAgICAgPC9vYmplY3Q+CiAgICAgIDwvdm9pZD4KICAgICA8L29iamVjdD4KICAgIDwvdm9pZD4KICAgIDx2b2lkIG1ldGhvZD0iYWRkIj4KICAgICA8b2JqZWN0IGNsYXNzPSJvcmcuZGF3bnNjaS5zbGljaW5nLmFwaS5zeXN0ZW0uRGltc0RhdGEiPgogICAgICA8dm9pZCBwcm9wZXJ0eT0iZGltZW5zaW9uIj4KICAgICAgIDxpbnQ+MzwvaW50PgogICAgICA8L3ZvaWQ+CiAgICAgIDx2b2lkIHByb3BlcnR5PSJwbG90QXhpcyI+CiAgICAgICA8b2JqZWN0IGNsYXNzPSJqYXZhLmxhbmcuRW51bSIgbWV0aG9kPSJ2YWx1ZU9mIj4KICAgICAgICA8Y2xhc3M+b3JnLmRhd25zY2kuc2xpY2luZy5hcGkuc3lzdGVtLkF4aXNUeXBlPC9jbGFzcz4KICAgICAgICA8c3RyaW5nPlk8L3N0cmluZz4KICAgICAgIDwvb2JqZWN0PgogICAgICA8L3ZvaWQ+CiAgICAgPC9vYmplY3Q+CiAgICA8L3ZvaWQ+CiAgIDwvb2JqZWN0PgogIDwvdm9pZD4KIDwvb2JqZWN0Pgo8L2phdmE+Cg==");
//    Map<String, String> props = new HashMap<String, String>();
//
//    flowMgr.executeBlockingLocally(flow, props);
//  }

  public void test2() throws Exception {
    Flow flow = new Flow("flow", null);
    new ETDirector(flow, "director");
    
    DataChunkSource src = new DataChunkSource(flow, "src");
    ClusterNodeTransformer cnt = new ClusterNodeTransformer(flow, "cnt");
    DevNullActor sink = new DevNullActor(flow, "sink");
    
    flow.connect(src, cnt);
    flow.connect(cnt,sink);
    sink.logReceivedMessages.setExpression("true");
    cnt.workflowFileParameter.setExpression("C:/temp/dls_trials/models/AnalysisMockFlow.moml");
//    src.datasetName.setExpression("set");
    src.datasetName.setExpression("/entry1/instrument/Pilatus2M/data");
    src.path.setExpression("C:/temp/dls_trials/input/i22-34774.nxs");
    src.relativePathParam.setExpression("false");
    src.slicing.setExpression("PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGphdmEgdmVyc2lvbj0iMS43LjBfNTEiIGNsYXNzPSJqYXZhLmJlYW5zLlhNTERlY29kZXIiPgogPG9iamVjdCBjbGFzcz0ib3JnLmRhd25zY2kuc2xpY2luZy5hcGkuc3lzdGVtLkRpbXNEYXRhTGlzdCI+CiAgPHZvaWQgcHJvcGVydHk9ImRpbXNEYXRhIj4KICAgPG9iamVjdCBjbGFzcz0iamF2YS51dGlsLkFycmF5TGlzdCI+CiAgICA8dm9pZCBtZXRob2Q9ImFkZCI+CiAgICAgPG9iamVjdCBjbGFzcz0ib3JnLmRhd25zY2kuc2xpY2luZy5hcGkuc3lzdGVtLkRpbXNEYXRhIj4KICAgICAgPHZvaWQgcHJvcGVydHk9ImRpbWVuc2lvbiI+CiAgICAgICA8aW50PjA8L2ludD4KICAgICAgPC92b2lkPgogICAgIDwvb2JqZWN0PgogICAgPC92b2lkPgogICAgPHZvaWQgbWV0aG9kPSJhZGQiPgogICAgIDxvYmplY3QgY2xhc3M9Im9yZy5kYXduc2NpLnNsaWNpbmcuYXBpLnN5c3RlbS5EaW1zRGF0YSI+CiAgICAgIDx2b2lkIHByb3BlcnR5PSJkaW1lbnNpb24iPgogICAgICAgPGludD4xPC9pbnQ+CiAgICAgIDwvdm9pZD4KICAgICAgPHZvaWQgcHJvcGVydHk9InBsb3RBeGlzIj4KICAgICAgIDxvYmplY3QgY2xhc3M9ImphdmEubGFuZy5FbnVtIiBtZXRob2Q9InZhbHVlT2YiPgogICAgICAgIDxjbGFzcz5vcmcuZGF3bnNjaS5zbGljaW5nLmFwaS5zeXN0ZW0uQXhpc1R5cGU8L2NsYXNzPgogICAgICAgIDxzdHJpbmc+UkFOR0U8L3N0cmluZz4KICAgICAgIDwvb2JqZWN0PgogICAgICA8L3ZvaWQ+CiAgICAgPC9vYmplY3Q+CiAgICA8L3ZvaWQ+CiAgICA8dm9pZCBtZXRob2Q9ImFkZCI+CiAgICAgPG9iamVjdCBjbGFzcz0ib3JnLmRhd25zY2kuc2xpY2luZy5hcGkuc3lzdGVtLkRpbXNEYXRhIj4KICAgICAgPHZvaWQgcHJvcGVydHk9ImRpbWVuc2lvbiI+CiAgICAgICA8aW50PjI8L2ludD4KICAgICAgPC92b2lkPgogICAgICA8dm9pZCBwcm9wZXJ0eT0icGxvdEF4aXMiPgogICAgICAgPG9iamVjdCBjbGFzcz0iamF2YS5sYW5nLkVudW0iIG1ldGhvZD0idmFsdWVPZiI+CiAgICAgICAgPGNsYXNzPm9yZy5kYXduc2NpLnNsaWNpbmcuYXBpLnN5c3RlbS5BeGlzVHlwZTwvY2xhc3M+CiAgICAgICAgPHN0cmluZz5YPC9zdHJpbmc+CiAgICAgICA8L29iamVjdD4KICAgICAgPC92b2lkPgogICAgIDwvb2JqZWN0PgogICAgPC92b2lkPgogICAgPHZvaWQgbWV0aG9kPSJhZGQiPgogICAgIDxvYmplY3QgY2xhc3M9Im9yZy5kYXduc2NpLnNsaWNpbmcuYXBpLnN5c3RlbS5EaW1zRGF0YSI+CiAgICAgIDx2b2lkIHByb3BlcnR5PSJkaW1lbnNpb24iPgogICAgICAgPGludD4zPC9pbnQ+CiAgICAgIDwvdm9pZD4KICAgICAgPHZvaWQgcHJvcGVydHk9InBsb3RBeGlzIj4KICAgICAgIDxvYmplY3QgY2xhc3M9ImphdmEubGFuZy5FbnVtIiBtZXRob2Q9InZhbHVlT2YiPgogICAgICAgIDxjbGFzcz5vcmcuZGF3bnNjaS5zbGljaW5nLmFwaS5zeXN0ZW0uQXhpc1R5cGU8L2NsYXNzPgogICAgICAgIDxzdHJpbmc+WTwvc3RyaW5nPgogICAgICAgPC9vYmplY3Q+CiAgICAgIDwvdm9pZD4KICAgICA8L29iamVjdD4KICAgIDwvdm9pZD4KICAgPC9vYmplY3Q+CiAgPC92b2lkPgogPC9vYmplY3Q+CjwvamF2YT4K");
    Map<String, String> props = new HashMap<String, String>();

    flowMgr.execute(flow, props);
  }

}
