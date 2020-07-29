package org.processmining.Guido.server;

import org.processmining.Guido.ConformanceChecker;
import org.processmining.Guido.DataAwareConformanceChecking.CheckVariableBoundsImporter;
import org.processmining.Guido.DataAwareConformanceChecking.Configs;
import org.processmining.Guido.DataAwareConformanceChecking.ControlFlowCost;
import org.processmining.Guido.DataAwareConformanceChecking.VariableMatchCostImporter;
import org.processmining.Guido.InOut.ControlFlowViolationCosts;
import org.processmining.Guido.InOut.Graph;
import org.processmining.Guido.InOut.VariableBoundsEntry;
import org.processmining.Guido.InOut.VariableMatchCostEntry;
import org.processmining.Guido.Result.AlignmentGroupResult;
import org.processmining.Guido.Result.GroupOutput;
import org.processmining.Guido.mapping.*;
import org.processmining.framework.plugin.Progress;
import org.processmining.graphvisualizers.plugins.GraphVisualizerPlugin;
import org.processmining.plugins.DataConformance.visualization.grouping.GroupedAlignments;
import org.processmining.plugins.balancedconformance.result.BalancedReplayResult;
import org.processmining.xesalignmentextension.XAlignmentExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CrossOrigin(origins = "*")
@Controller
public class ConfigsController {

    ConformanceChecker cc = Database.getConformanceChecker();

    @GetMapping("/params")
    public ResponseEntity<String> getParams() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(cc.getDefaultParameters().toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Failed to get!");
        }
    }

    @PostMapping("/params")
    public ResponseEntity<String> postParams(@RequestBody Configs c) {
        try {
            cc.setConfigs(c);
            return ResponseEntity.status(HttpStatus.OK).body("all ok");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Failed to post!");
        }
    }


    // TODO: change getModelDPN in getModel
    @GetMapping("/initialMapping")
    public ResponseEntity<InitialMapping> getInitialMapping() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(cc.getInitialMapping());
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
        }
    }

    @PostMapping("/finalMapping")
    public ResponseEntity<String> postFinalMapping(@RequestBody FinalMapping finalMapping) {
        try {
            cc.setMapping(finalMapping);
            return ResponseEntity.status(HttpStatus.OK).body("all ok");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Failed to post!");
        }
    }

    @GetMapping("/controlFlowCost")
    public ResponseEntity<ControlFlowViolationCosts> queryControlFlowCost() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(cc.queryControlFlowCost());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
        }
    }

    @PostMapping("/controlFlowCost")
    public ResponseEntity<String> postControlFlowCost(@RequestBody ControlFlowViolationCosts costs) {
        try {
            cc.postControlFlowCost(costs);
            return ResponseEntity.status(HttpStatus.OK).body("All ok");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("The operation was interrupted");
        }
    }

    @GetMapping("/initialVariableMapping")
    public ResponseEntity<InitialVariableMapping> getInitialVariableMapping() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(cc.getInitialVariableMapping());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
        }
    }

    @PostMapping("/postVariableMapping")
    public ResponseEntity<String> postFinalVariableMapping(@RequestBody Map<String, String> map) {
        try {
            cc.setVariableMapping(map);
            return ResponseEntity.status(HttpStatus.OK).body("all ok");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Failed to post!");
        }
    }

    @GetMapping("/variableMatchCost")
    public ResponseEntity<VariableMatchCostImporter.Input> queryVariableMatchCost() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(cc.queryVariableMathCost());
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
        }
    }

    @PostMapping("/variableMatchCost")
    public ResponseEntity<String> postVariableMatchCost(@RequestBody List<VariableMatchCostEntry> entries) {
        try {
            cc.postVariableMatchCost(entries);
            return ResponseEntity.status(HttpStatus.OK).body("All ok");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("The operation was interrupted");
        }
    }

    @GetMapping("/variableBounds")
    public ResponseEntity<CheckVariableBoundsImporter> queryVariableBounds() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(cc.queryVariableBounds());
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
        }
    }

    @PostMapping("/variableBounds")
    public ResponseEntity<String> postVariableBounds(@RequestBody List<VariableBoundsEntry> list) {
        try {
            cc.postVariableBounds(list);

            return ResponseEntity.status(HttpStatus.OK).body("All ok");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("The operation was interrupted");
        }
    }

    @GetMapping("/customDpnConversion")
    public ResponseEntity<String> customDpnConversion() {
        try {
            cc.customDpnConversion();

            return ResponseEntity.status(HttpStatus.OK).body("Operation ended without errors");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("The operation was interrupted");
        }
    }

    @GetMapping("/queryConfiguration")
    public ResponseEntity<String> queryConfiguration() {
        try {
            if(Database.hasStarted()) {
                // do nothing
            }
            else
                cc.queryConfiguration();

            return ResponseEntity.status(HttpStatus.OK).body("Operation ended without errors");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("The operation was interrupted");
        }
    }

    @GetMapping("/balancedDataConformance")
    public ResponseEntity<String> balancedDataConformance() {
        try {
            if(Database.hasStarted()) {
                // do nothing
            }
            else {
                Database.setStarted(true);
                cc.dobBlancedDataConformance();
            }

            return ResponseEntity.status(HttpStatus.OK).body("WAITING");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
        }
    }

    @GetMapping("/progress")
    public ResponseEntity<Object[]> getProgress() {
        try {
            Object[] data = cc.getProgress();
            if((boolean) data[1])
                Database.setStarted(false);
            return ResponseEntity.status(HttpStatus.OK).body(cc.getProgress());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
        }
    }

    @GetMapping("/groups")
    public ResponseEntity<AlignmentGroupResult> getGroups() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(cc.getGroups());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
        }
    }

    @GetMapping("/dot")
    public ResponseEntity<String> dot() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(cc.renderDot2());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
        }
    }

    @GetMapping("/dotInitial")
    public ResponseEntity<Graph> dotInitial() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(cc.renderDot(0));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
        }
    }

    @GetMapping("/dotCustom")
    public ResponseEntity<Graph> dotCustom() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(cc.renderDot(1));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
        }
    }

    @GetMapping("/dotFinal")
    public ResponseEntity<Graph> dotFinal() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(cc.renderDot(2));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
        }
    }

//    @GetMapping("/bpmnDot")
//    public ResponseEntity<String> bpmnDot() {
//        try {
//            return ResponseEntity.status(HttpStatus.OK).body(cc.renderBpmnDot());
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
//        }
//    }

}
