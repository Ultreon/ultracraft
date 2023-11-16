package com.ultreon.craft.client.init;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.ultreon.craft.client.render.shader.OpenShaderProvider;

class MyDefaultShaderProvider extends DefaultShaderProvider implements OpenShaderProvider {
    public MyDefaultShaderProvider(DefaultShader.Config config) {
        super(config);
    }

    public MyDefaultShaderProvider(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    public MyDefaultShaderProvider(FileHandle vertexShader, FileHandle fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    public MyDefaultShaderProvider() {
    }

    @Override
    public Shader createShader(Renderable renderable) {
        DefaultShader defaultShader = new DefaultShader(renderable, this.config) {
            {
                System.out.println("this.program = " + this.program);
                System.out.println("this.toString() = " + this.toString());
            }

            @Override
            public void init() {
                System.out.println("this.toString() = " + this.toString());
                System.out.println("this.program = " + this.program);
                super.init();
                System.out.println("this.program = " + this.program);
                System.out.println("this.toString() = " + this.toString());
            }

            @Override
            public void init(ShaderProgram program, Renderable renderable) {
                System.out.println("this.toString() = " + this.toString());
                System.out.println("this.program = " + this.program);
                super.init(program, renderable);
                System.out.println("this.program = " + this.program);
                System.out.println("this.toString() = " + this.toString());
            }

            @Override
            public String toString() {
                return String.valueOf(Integer.toHexString(hashCode()) + ", " + this.program);
            }
        };

        Shaders.checkShaderCompilation(defaultShader.program);

        return defaultShader;
    }

}
