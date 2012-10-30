/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2012.                            (c) 2012.
*  Government of Canada                 Gouvernement du Canada
*  National Research Council            Conseil national de recherches
*  Ottawa, Canada, K1A 0R6              Ottawa, Canada, K1A 0R6
*  All rights reserved                  Tous droits réservés
*                                       
*  NRC disclaims any warranties,        Le CNRC dénie toute garantie
*  expressed, implied, or               énoncée, implicite ou légale,
*  statutory, of any kind with          de quelque nature que ce
*  respect to the software,             soit, concernant le logiciel,
*  including without limitation         y compris sans restriction
*  any warranty of merchantability      toute garantie de valeur
*  or fitness for a particular          marchande ou de pertinence
*  purpose. NRC shall not be            pour un usage particulier.
*  liable in any event for any          Le CNRC ne pourra en aucun cas
*  damages, whether direct or           être tenu responsable de tout
*  indirect, special or general,        dommage, direct ou indirect,
*  consequential or incidental,         particulier ou général,
*  arising from the use of the          accessoire ou fortuit, résultant
*  software.  Neither the name          de l'utilisation du logiciel. Ni
*  of the National Research             le nom du Conseil National de
*  Council of Canada nor the            Recherches du Canada ni les noms
*  names of its contributors may        de ses  participants ne peuvent
*  be used to endorse or promote        être utilisés pour approuver ou
*  products derived from this           promouvoir les produits dérivés
*  software without specific prior      de ce logiciel sans autorisation
*  written permission.                  préalable et particulière
*                                       par écrit.
*                                       
*  This file is part of the             Ce fichier fait partie du projet
*  OpenCADC project.                    OpenCADC.
*                                       
*  OpenCADC is free software:           OpenCADC est un logiciel libre ;
*  you can redistribute it and/or       vous pouvez le redistribuer ou le
*  modify it under the terms of         modifier suivant les termes de
*  the GNU Affero General Public        la “GNU Affero General Public
*  License as published by the          License” telle que publiée
*  Free Software Foundation,            par la Free Software Foundation
*  either version 3 of the              : soit la version 3 de cette
*  License, or (at your option)         licence, soit (à votre gré)
*  any later version.                   toute version ultérieure.
*                                       
*  OpenCADC is distributed in the       OpenCADC est distribué
*  hope that it will be useful,         dans l’espoir qu’il vous
*  but WITHOUT ANY WARRANTY;            sera utile, mais SANS AUCUNE
*  without even the implied             GARANTIE : sans même la garantie
*  warranty of MERCHANTABILITY          implicite de COMMERCIALISABILITÉ
*  or FITNESS FOR A PARTICULAR          ni d’ADÉQUATION À UN OBJECTIF
*  PURPOSE.  See the GNU Affero         PARTICULIER. Consultez la Licence
*  General Public License for           Générale Publique GNU Affero
*  more details.                        pour plus de détails.
*                                       
*  You should have received             Vous devriez avoir reçu une
*  a copy of the GNU Affero             copie de la Licence Générale
*  General Public License along         Publique GNU Affero avec
*  with OpenCADC.  If not, sesrc/jsp/index.jspe          OpenCADC ; si ce n’est
*  <http://www.gnu.org/licenses/>.      pas le cas, consultez :
*                                       <http://www.gnu.org/licenses/>.
*
*  $Revision: 4 $
*
************************************************************************
*/

package ca.nrc.cadc.vos.client.ui;

import org.apache.log4j.Logger;

import ca.nrc.cadc.vos.client.VOSpaceClient;

/**
 * Executes the commands in the command queue.
 * 
 * @author majorb
 *
 */
public class CommandExecutor implements Runnable
{
    
    protected static final Logger log = Logger.getLogger(CommandExecutor.class);
    
    private VOSpaceClient vospaceClient;
    private CommandQueue queue;
    
    CommandExecutor(VOSpaceClient vospaceClient, CommandQueue queue)
    {
        this.vospaceClient = vospaceClient;
        this.queue = queue;
    }
    
    /**
     * Executes the commands in the queue until queue production is
     * complete or aborted.
     */
    public void run()
    {
        queue.startedConsumption();
        try
        {
            VOSpaceCommand nextCommand;
            while (!queue.isAbortedProduction()
                   && (!queue.isDoneProduction() || (queue.peek() != null)))
            {
                while (!queue.isAbortedProduction()
                       && ((nextCommand = queue.peek()) != null))
                {
                    log.debug("Executing command: " + nextCommand);
                    try
                    {
                        nextCommand.execute(vospaceClient);
                    }
                    catch (Exception e)
                    {
                        log.info("Error executing command: " + nextCommand
                                 + ": " + e);
                    }
                    queue.remove();
                }
                
                // sleep for a while
                Thread.sleep(10);
            }
        }
        catch (InterruptedException e)
        {
            // exit
        }
        finally
        {
            //
            // TODO - If a CommandQueue is aborted, is it also considered
            // TODO - to be 'Done'?  It seems unlikely.
            //
            // TODO - jenkinsd 2012.10.23
            //
            if (!queue.isAbortedProduction())
            {
                queue.doneConsumption();
            }

            // Clean up.
            queue.clear();
        }
    }
}