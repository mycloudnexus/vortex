import { fixupConfigRules } from '@eslint/compat'
import prettier from 'eslint-plugin-prettier'
import globals from 'globals'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import js from '@eslint/js'
import { FlatCompat } from '@eslint/eslintrc'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const compat = new FlatCompat({
  baseDirectory: __dirname,
  recommendedConfig: js.configs.recommended,
  allConfig: js.configs.all
})

export default [
  {
    ignores: ['**/node_modules/', '**/dist/']
  },
  ...fixupConfigRules(
    compat.extends(
      'eslint:recommended',
      'plugin:react/recommended',
      'plugin:react-hooks/recommended',
      'plugin:import/recommended',
      'plugin:jsx-a11y/recommended',
      'plugin:@typescript-eslint/recommended',
      'eslint-config-prettier',
      'prettier'
    )
  ),
  {
    plugins: {
      prettier
    },

    languageOptions: {
      globals: {
        ...globals.node,
        window: 'writable',
        document: 'writable',
        __webpack_init_sharing__: 'readonly',
        __webpack_share_scopes__: 'readonly'
      }
    },

    settings: {
      react: {
        version: 'detect'
      },
      'import/resolver': {
        node: {
          paths: [__dirname],
          extensions: ['.js', '.jsx', '.ts', '.tsx']
        },
        typescript: {
          project: `${__dirname}/tsconfig.json`
        }
      }
    },

    rules: {
      'react/react-in-jsx-scope': 'off',
      'react/jsx-no-target-blank': 'warn',
      '@typescript-eslint/no-explicit-any': 'off',
      'jsx-a11y/anchor-is-valid': 0,
      'react-hooks/exhaustive-deps': 'off',

      'prettier/prettier': [
        'warn',
        {
          arrowParens: 'always',
          semi: false,
          trailingComma: 'none',
          tabWidth: 2,
          endOfLine: 'auto',
          useTabs: false,
          singleQuote: true,
          printWidth: 120,
          jsxSingleQuote: true
        }
      ],

      'jsx-a11y/click-events-have-key-events': 'off',
      'jsx-a11y/no-static-element-interactions': 'off',

      '@typescript-eslint/no-unused-vars': [
        'warn',
        {
          varsIgnorePattern: '[iI]gnored',
          argsIgnorePattern: '[iI]gnored'
        }
      ]
    }
  }
]
