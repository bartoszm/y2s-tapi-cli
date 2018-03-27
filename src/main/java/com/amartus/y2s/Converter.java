/*
 * Copyright (c) 2018 Amartus. All rights reserved.
 *  This program and the accompanying materials are made available under the
 *  terms of the Eclipse Public License v1.0 which accompanies this distribution,
 *  and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Bartosz Michalik <bartosz.michalik@amartus.com>
 */
package com.amartus.y2s;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mrv.yangtools.codegen.SwaggerGenerator;
import com.mrv.yangtools.codegen.impl.postprocessor.RemoveUnusedDefinitions;
import com.mrv.yangtools.codegen.impl.postprocessor.SingleParentInheritenceModel;
import com.mrv.yangtools.codegen.impl.postprocessor.SortComplexModels;
import com.mrv.yangtools.common.SwaggerUtils;
import io.swagger.models.Swagger;
import io.swagger.parser.Swagger20Parser;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.*;

/**
 * @author bartosz.michalik@amartus.com
 */
public class Converter {
    @Option(name = "-output", usage = "File to generate, containing the output - defaults to stdout", metaVar = "file")
    public String output = "";

    @Option(name = "-input", usage = "File with original swagger, containing the input - defaults to stdin", metaVar = "file")
    public String input = "";

    @Option(name="-format", usage = "Define format for swagger input/output")
    public SwaggerGenerator.Format format = SwaggerGenerator.Format.YAML;
    private ObjectMapper mapper;


    public static void main(String[] args) throws IOException {
        Converter cli = new Converter();
        CmdLineParser parser = new CmdLineParser(cli);
        try {
            parser.parseArgument(args);
            cli.convert();
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
        }


    }

    private void convert() throws IOException {
        mapper = format == SwaggerGenerator.Format.JSON ?  new ObjectMapper(new JsonFactory())
                : new ObjectMapper(new YAMLFactory());

        try(
                Reader in =  "".equals(input) ? new InputStreamReader(System.in) : new FileReader(input);
                Writer out = "".equals(output) ? new OutputStreamWriter(System.out) : new FileWriter(output)
        ) {
            JsonNode jsonNode = mapper.reader().readTree(in);

            Swagger swagger = new Swagger20Parser().read(jsonNode);
            new SingleParentInheritenceModel().andThen(new RemoveUnusedDefinitions()).accept(swagger);

            new SortComplexModels().accept(swagger);

            swagger.setDefinitions(SwaggerUtils.sortMap(swagger.getDefinitions()));
            swagger.setPaths(SwaggerUtils.sortMap(swagger.getPaths()));

            write(out, swagger);

        }
    }

    private void write(Writer target, Swagger swagger) throws IOException {

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.writeValue(target, swagger);
    }

}
