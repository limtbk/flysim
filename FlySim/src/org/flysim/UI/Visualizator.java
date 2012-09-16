package org.flysim.UI;

import java.awt.Color;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

import org.flysim.Simulator.Simulator;

import com.sun.j3d.utils.behaviors.keyboard.KeyNavigatorBehavior;
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.universe.SimpleUniverse;

public class Visualizator implements Runnable {
	private Simulator simulator;
	
    private SimpleUniverse universe;
    private Appearance copterEdgesAppearance;
    private BranchGroup rootGroup;
    private BoundingSphere bounds;

	private Appearance copterMainEngineAppearance;

	private Appearance copterEngineAppearance;

	private Appearance groundOddAppearance;

	private Appearance groundEvenAppearance;
	private Vector3f copterCoord;

	private Transform3D copterTransform;

	private TransformGroup copterTransformGroup;
	
	public Visualizator(Simulator s) {
		simulator = s;
	}
	
	public void run() {
		while (true) {
			getCopterCoord();
			//System.out.println("VT");
		}
	}
	
    private void getCopterCoord() {
    	copterCoord.x = (float)simulator.position.x;
    	copterCoord.y = (float)simulator.position.y;
    	copterCoord.z = (float)simulator.position.z;
//    	copterTransform.setTranslation(copterCoord);
//        copterTransformGroup.setTransform(copterTransform);
    }

	public void init() {
        createUniverse();
        createAppearance();
        //createCubeOfCubes();
        
        createGround();
        createCopter();

        createLights();
        createBehaviourInteractors();
        universe.getViewingPlatform().setNominalViewingTransform();

        // add the cube group of objects to SimpleUnvirse object
        universe.addBranchGraph(rootGroup);
    }
    
    private void createUniverse() {
        universe = new SimpleUniverse();
        rootGroup = new BranchGroup();
        bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
        
    }

    private void setupAppearance(Appearance app, Color col) {
        Color3f ambientColour = new Color3f();
        ambientColour.set(col);
        Color3f emissiveColour = new Color3f(0.0f, 0.0f, 0.0f);
        Color3f specularColour = new Color3f(1.0f, 1.0f, 1.0f);
        Color3f diffuseColour = new Color3f();
        diffuseColour.set(col);
        float shininess = 20.0f;
        app.setMaterial(new Material(ambientColour, emissiveColour,
                diffuseColour, specularColour, shininess));
    }
    
    private void createAppearance() {
        copterEdgesAppearance = new Appearance();
        setupAppearance(copterEdgesAppearance, Color.GRAY);

        copterMainEngineAppearance = new Appearance();
        setupAppearance(copterMainEngineAppearance, Color.RED);

        copterEngineAppearance = new Appearance();
        setupAppearance(copterEngineAppearance, Color.GREEN);
        
        groundOddAppearance = new Appearance();
        setupAppearance(groundOddAppearance, Color.DARK_GRAY);
        groundEvenAppearance = new Appearance();
        setupAppearance(groundEvenAppearance, Color.BLACK);
    }
    
    private void createGround() {
        TransformGroup tg = new TransformGroup();
        Transform3D transform = new Transform3D();
        Vector3f vector = new Vector3f(0f, 0f, 0f);
        transform.setTranslation(vector);
        tg.setTransform(transform);
        rootGroup.addChild(tg);

    	for (int x = -20; x <= 20; x++) {
			for (int y = -20; y <= 20; y++) {
		    	Box box = new Box(0.5f, 0.5f, 0.001f, (((x+y)%2)!=0) ? groundOddAppearance : groundEvenAppearance);
		    	tg.addChild(addNodeAtCoord(box, x*1.0f, y*1.0f, 0f));
			}
		}
    }

    private Node addNodeAtCoord(Node box, float x, float y, float z) {
        TransformGroup tg = new TransformGroup();
        Transform3D transform = new Transform3D();
        Vector3f vector = new Vector3f(x, y, z);
        transform.setTranslation(vector);
        tg.setTransform(transform);
        tg.addChild(box);
        
        return tg;
//        rootGroup.addChild(tg);
    }
    
    private void createCopter() {
    	
        TransformGroup tg = new TransformGroup();
        Transform3D transform = new Transform3D();
        Vector3f vector = new Vector3f(0f, 0f, 1.0f);
        copterCoord = vector;
        transform.setTranslation(vector);
        copterTransform = transform;
        tg.setTransform(transform);
        copterTransformGroup = tg;
        rootGroup.addChild(tg);
    	
    	Box boxLeftEngine = new Box(0.05f, 0.05f, 0.05f, copterEngineAppearance);
    	tg.addChild(addNodeAtCoord(boxLeftEngine, -0.25f, 0f, 0f));
    	Box boxRightEngine = new Box(0.05f, 0.05f, 0.05f, copterEngineAppearance); 
    	tg.addChild(addNodeAtCoord(boxRightEngine, 0.25f, 0f, 0f));
    	Box boxForwardEngine = new Box(0.05f, 0.05f, 0.05f, copterMainEngineAppearance); 
    	tg.addChild(addNodeAtCoord(boxForwardEngine, 0f, 0.25f, 0f));
    	Box boxBackwardEngine = new Box(0.05f, 0.05f, 0.05f, copterEngineAppearance); 
    	tg.addChild(addNodeAtCoord(boxBackwardEngine, 0f, -0.25f, 0f));
    	
    	Box boxFwBw = new Box(0.005f, 0.25f, 0.005f, copterEdgesAppearance);
    	tg.addChild(addNodeAtCoord(boxFwBw, 0f, 0f, 0f));
    	Box boxLfRg = new Box(0.25f, 0.005f, 0.005f, copterEdgesAppearance);
    	tg.addChild(addNodeAtCoord(boxLfRg, 0f, 0f, 0f));
    	
    }
    
    private void createLights() {
        Color3f ambientLightColour = new Color3f(0.9f, 0.9f, 0.9f);
        AmbientLight ambientLight = new AmbientLight(ambientLightColour);
        ambientLight.setInfluencingBounds(bounds);
        Color3f directionLightColour = new Color3f(1.0f, 1.0f, 1.0f);
        Vector3f directionLightDir = new Vector3f(-1.0f, -1.0f, -1.0f);
        DirectionalLight directionLight = new DirectionalLight(directionLightColour, directionLightDir);
        directionLight.setInfluencingBounds(bounds);
        rootGroup.addChild(ambientLight);
        rootGroup.addChild(directionLight);
    }
    
    private void createBehaviourInteractors() {
        TransformGroup viewTransformGroup =
                universe.getViewingPlatform().getViewPlatformTransform();

        KeyNavigatorBehavior keyInteractor =
                new KeyNavigatorBehavior(viewTransformGroup);

        BoundingSphere movingBounds = new BoundingSphere(new Point3d(0.0, 0.0,
        0.0), 100.0);
        keyInteractor.setSchedulingBounds(movingBounds);
        rootGroup.addChild(keyInteractor);

        MouseRotate behavior = new MouseRotate();
        behavior.setTransformGroup(viewTransformGroup);
        rootGroup.addChild(behavior);
        behavior.setSchedulingBounds(bounds);
    }

}