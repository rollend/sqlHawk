/*
 * This file is a part of the SchemaSpy project (http://schemaspy.sourceforge.net).
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010 John Currier
 *
 * SchemaSpy is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package net.sourceforge.schemaspy;

import net.sourceforge.schemaspy.db.read.ConnectionFailure;
import net.sourceforge.schemaspy.db.read.EmptySchemaException;
import net.sourceforge.schemaspy.db.read.ProcessExecutionException;
import net.sourceforge.schemaspy.ui.MainFrame;

/**
 * @author John Currier
 */
public class Main {
    public static void main(String[] argv) throws Exception {
        if (argv.length == 1 && argv[0].equals("-gui")) { // warning: serious temp hack
            new MainFrame().setVisible(true);
            return;
        }
        
        //print welcome message to console
        String version = Main.class.getPackage().getImplementationVersion();
        if (version!=null) //will be null if run outside package, i.e. in eclipse.
        	System.out.println("sqlHawk " + Main.class.getPackage().getImplementationVersion());
        System.out.println("More information at http://timwise.wikispaces.com/sqlHawk");
        System.out.println();

        //load config
        Config config = new Config(argv);

        int exitCode = 1;

        try {
	        //begin analysis
	        SchemaAnalyzer analyzer = new SchemaAnalyzer();
            exitCode = analyzer.analyze(config) == null ? 1 : 0;
        } catch (ConnectionFailure couldntConnect) {
            // failure already logged
            exitCode = 3;
        } catch (EmptySchemaException noData) {
            // failure already logged
            exitCode = 2;
        } catch (InvalidConfigurationException badConfig) {
            System.err.println();
            if (badConfig.getParamName() != null)
                System.err.println("Bad parameter specified for " + badConfig.getParamName());
            System.err.println(badConfig.getMessage());
            if (badConfig.getCause() != null && !badConfig.getMessage().endsWith(badConfig.getMessage()))
                System.err.println(" caused by " + badConfig.getCause().getMessage());
        } catch (ProcessExecutionException badLaunch) {
            System.err.println(badLaunch.getMessage());
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        System.exit(exitCode);
    }
}