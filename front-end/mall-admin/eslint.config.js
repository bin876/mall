import globals from 'globals'
import pluginJS from '@eslint/js'
import pluginTS from 'typescript-eslint'
import pluginVue from 'eslint-plugin-vue'

import tsParser from '@typescript-eslint/parser'
import vueParser from 'vue-eslint-parser'

import configPrettier from 'eslint-config-prettier'

const baseParserOptions = { ecmaVersion: 'latest', sourceType: 'module' }

export default [
  pluginJS.configs.recommended,
  ...pluginTS.configs.recommended,
  ...pluginVue.configs['flat/essential'],
  configPrettier,

  {
    files: ['**/*.{js,ts,vue}'],
    languageOptions: {
      globals: { ...globals.browser, ...globals.node },
      parserOptions: baseParserOptions,
    },
  },

  {
    files: ['**/*.ts'],
    languageOptions: {
      parser: tsParser,
      parserOptions: baseParserOptions,
    },
    rules: {},
  },

  {
    files: ['**/*.vue'],
    languageOptions: {
      parser: vueParser,
      parserOptions: {
        parser: tsParser,
        ...baseParserOptions,
      },
    },
    rules: {
      'vue/multi-word-component-names': 'off',
    },
  },
]
